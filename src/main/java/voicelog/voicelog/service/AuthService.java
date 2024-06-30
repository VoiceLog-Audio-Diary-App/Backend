package voicelog.voicelog.service;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import voicelog.voicelog.domain.EmailCertification;
import voicelog.voicelog.domain.RefreshToken;
import voicelog.voicelog.dto.request.*;
import voicelog.voicelog.dto.response.*;
import voicelog.voicelog.dto.response.auth.*;
import voicelog.voicelog.provider.EmailProvider;
import voicelog.voicelog.repository.EmailCertificationRepository;
import voicelog.voicelog.repository.RefreshTokenRepository;
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

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JavaMailSender javaMailSender;
    private static final String senderEmail= "lsj90954511@gmail.com";

    private final EmailProvider emailProvider;
    private final EmailCertificationRepository emailCertificationRepository;

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

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

            boolean isExist = userRepository.existsByUsername(email);
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
            boolean isExist = userRepository.existsByUsername(email);
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
            boolean isExist = userRepository.existsByUsername(email);
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
            User user = userRepository.findByUsername(email);
            if (user == null || user.getStatus() != 1)
                return SignInResponseDto.signInFail();

            String password = dto.getPassword();
            String encodedPassword = user.getPassword();
            boolean isMatched = passwordEncoder.matches(password, encodedPassword);
            if (!isMatched)
                return SignInResponseDto.signInFail();

            accessToken = jwtUtil.createJwt(email, 1000 * 60 * 15L);
            refreshToken = jwtUtil.createJwt(email, 1000 * 60 * 60 * 24 * 30L);

            //리프레시토큰 저장
            RefreshToken refreshToken1 = new RefreshToken();
            refreshToken1.setUser(user);
            refreshToken1.setRefreshToken(refreshToken);
            refreshToken1.setExpiredDate(LocalDateTime.now().plus(1000 * 60 * 60 * 24 * 30L, ChronoUnit.MILLIS));
            refreshToken1.setCreatedDate(LocalDateTime.now());

            refreshTokenRepository.save(refreshToken1);

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
            String refreshToken = dto.getRefreshToken();

            newAccessToken = refreshAccessToken(refreshToken);

            if (newAccessToken == null)
                return RefreshAccessTokenResponseDto.refreshFail();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }
        String email = jwtUtil.getUsername(newAccessToken);
        User user = userRepository.findByUsername(email);

        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByUser(user);
        RefreshToken token = optionalToken.get();
        return RefreshAccessTokenResponseDto.success(newAccessToken, token.getRefreshToken());
    }

    public String refreshAccessToken(String refreshToken) {
        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByRefreshToken(refreshToken);

        if (optionalToken.isPresent()) {
            RefreshToken token = optionalToken.get();

            //리프레시 토큰 만료 시
            if (token.getExpiredDate().isBefore(LocalDateTime.now())) {
                User user = token.getUser();

                String newRefreshToken = jwtUtil.createJwt(user.getUsername(), 1000 * 60 * 60 * 24 * 30L);//30일
                token.setRefreshToken(newRefreshToken);
                token.setExpiredDate(LocalDateTime.now().plus(1000 * 60 * 60 * 24 * 30L, ChronoUnit.MILLIS));
                token.setCreatedDate(LocalDateTime.now());

                refreshTokenRepository.save(token);

                String newAccessToken = jwtUtil.createJwt(user.getUsername(), 1000 * 60 * 15L); // 15분
                return newAccessToken;
            } else {
                //리프레시 토큰 유효 시
                String newAccessToken = jwtUtil.createJwt(token.getUser().getUsername(), 1000 * 60 * 15L); // 15분
                return newAccessToken;
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }
    }
}
