package voicelog.voicelog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import voicelog.voicelog.domain.EmailCertification;
import voicelog.voicelog.dto.request.auth.*;
import voicelog.voicelog.dto.response.*;
import voicelog.voicelog.dto.response.auth.*;
import voicelog.voicelog.provider.EmailProvider;
import voicelog.voicelog.repository.EmailCertificationRepository;
import voicelog.voicelog.repository.UserRepository;
import voicelog.voicelog.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import voicelog.voicelog.utils.JwtUtil;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final EmailProvider emailProvider;
    private final EmailCertificationRepository emailCertificationRepository;

    private final JwtUtil jwtUtil;

    private final RedisTemplate<String, String> redisTemplate;

    // 6자리 인증코드 생성
    private String generateValidationCode() {
        Random rand = new Random();
        int number = rand.nextInt(999999);

        return String.format("%06d", number);
    }

    //회원가입
    public ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto) {
        try {
            String email = dto.getEmail();
            String certificationNumber = dto.getCertificationNumber();

            boolean isExist = userRepository.existsByUsernameAndStatus(email, 1);
            if (isExist)
                return SignUpResponseDto.duplicatedEmail();

            EmailCertification emailCertification = emailCertificationRepository.findByEmail(email);
            boolean isMatched = emailCertification.getEmail().equals(email) && emailCertification.getCertificationNumber().equals(certificationNumber);

            if (!isMatched)
                return SignUpResponseDto.certificationFail();

            String password = dto.getPassword();
            password = passwordEncoder.encode(password);
            dto.setPassword(password);

            User user = new User(dto);
            userRepository.save(user);

            emailCertificationRepository.deleteByEmail(email);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }
        return SignUpResponseDto.success();
    }

    //이메일 중복 체크
    public ResponseEntity<? super EmailCheckResponseDto> emailCheck(EmailCheckRequestDto dto) {
        try {
            String email = dto.getEmail();
            boolean isExist = userRepository.existsByUsernameAndStatus(email, 1);
            if (isExist)
                return EmailCheckResponseDto.duplicatedEmail();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return EmailCheckResponseDto.success();
    }

    //이메일 보내기
    public ResponseEntity<? super EmailCertificationResponseDto> emailCertification(EmailCertificationRequestDto dto) {
        try {
            String email = dto.getEmail();
            boolean isExist = userRepository.existsByUsernameAndStatus(email, 1);
            if (isExist)
                return EmailCheckResponseDto.duplicatedEmail();

            String certificationNumber = generateValidationCode();

            boolean isSuccess = emailProvider.sendCertificationMail(email, certificationNumber);

            if (!isSuccess)
                return EmailCertificationResponseDto.mailSendFail();

            EmailCertification emailCertification = new EmailCertification(email, certificationNumber);
            emailCertificationRepository.save(emailCertification);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }
        return EmailCertificationResponseDto.success();
    }

    //인증번호 확인
    public ResponseEntity<? super CertificationCheckResponseDto> certificationCheck(CertificationCheckRequestDto dto) {
        try {
            String email = dto.getEmail();
            String certificationNumber = dto.getCertificationNumber();

            EmailCertification emailCertification = emailCertificationRepository.findByEmail(email);

            if (emailCertification == null)
                return CertificationCheckResponseDto.certificationFail();

            boolean isMatched = emailCertification.getEmail().equals(email) && emailCertification.getCertificationNumber().equals(certificationNumber);

            if (!isMatched)
                return CertificationCheckResponseDto.certificationFail();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }
        return CertificationCheckResponseDto.success();
    }

    //로그인
    public ResponseEntity<? super SignInResponseDto> signIn(SignInRequestDto dto) {
        String accessToken = null;
        String refreshToken = null;

        try {
            String email = dto.getEmail();
            User user = userRepository.findByUsernameAndStatus(email, 1);
            if (user == null || user.getStatus() != 1)
                return SignInResponseDto.signInFail();

            String password = dto.getPassword();
            String encodedPassword = user.getPassword();
            boolean isMatched = passwordEncoder.matches(password, encodedPassword);
            if (!isMatched)
                return SignInResponseDto.signInFail();

            accessToken = jwtUtil.createJwt(email, 1000 * 60 * 15L);
            refreshToken = jwtUtil.createJwt(email, 1000 * 60 * 60 * 24 * 30L);

            //리프레시토큰 저장/갱신
            long expiredTime = 1000L * 60 * 60 * 24 * 30;
            String redisKey = "RefreshToken:" + email;
            redisTemplate.opsForValue().set(redisKey, refreshToken, expiredTime, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }
        System.out.println(accessToken);
        return SignInResponseDto.success(accessToken, refreshToken);
    }

    //액세스토큰 재발급
    public ResponseEntity<? super RefreshAccessTokenResponseDto> refreshAccessToken(RefreshAccessTokenRequestDto dto) {

        String newAccessToken = null;

        try {

            newAccessToken = refreshAccessToken(dto.getEmail());

            if (newAccessToken == null)
                return RefreshAccessTokenResponseDto.refreshFail();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }
        String email = dto.getEmail();

        String redisKey = "RefreshToken:" + email;
        String refreshToken = redisTemplate.opsForValue().get(redisKey);

        return RefreshAccessTokenResponseDto.success(newAccessToken, refreshToken);
    }

    public String refreshAccessToken(String email) {

        String redisKey = "RefreshToken:" + email;
        String refreshToken = redisTemplate.opsForValue().get(redisKey);

        //리프레시 토큰 유효시
        if (refreshToken != null) {
            String newAccessToken = jwtUtil.createJwt(email, 1000 * 60 * 15L); // 15분
            return newAccessToken;
        } else {
            //리프레시토큰 생성
            String newRefreshToken = jwtUtil.createJwt(email, 1000 * 60 * 60 * 24 * 30L);

            long expiredTime = 1000L * 60 * 60 * 24 * 30;
            String redisKey2 = "RefreshToken:" + email;
            redisTemplate.opsForValue().set(redisKey2, newRefreshToken, expiredTime, TimeUnit.MILLISECONDS);

            String newAccessToken = jwtUtil.createJwt(email, 1000 * 60 * 15L); // 15분
            return newAccessToken;
        }
    }

    public ResponseEntity<? super SignOutResponseDto> signOut(String token, String email) {
        try {
            //액세스 토큰 블랙리스트에 추가
            long remainingTime = jwtUtil.getRemainingExpiration(token);
            String redisKey = "Blacklist:" + token;
            redisTemplate.opsForValue().set(redisKey, "blacklisted", remainingTime, TimeUnit.SECONDS);

            //리프레시 토큰 삭제
            String redisKey2 = "RefreshToken:" + email;
            redisTemplate.delete(redisKey2);
        } catch (Exception e) {
            e.printStackTrace();
            return SignOutResponseDto.databaseError();
        }
        return SignOutResponseDto.success();
    }
}
