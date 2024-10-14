package voicelog.voicelog.service;

import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
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

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String CLIENT_SECRET;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String CLIENT_ID;

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

    //소셜로그인/회원가입
    public ResponseEntity<? super OAuth2ResponseDto> oAuth2(OAuth2RequestDto dto) {
        String accessToken = null;
        String refreshToken = null;
        String code = dto.getCode();
        String state = dto.getState();
        String naverAccessToken = null;
        String email = null;
        try {
            String reqUrl = "https://nid.naver.com/oauth2.0/token";
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", CLIENT_ID);
            params.add("client_secret", CLIENT_SECRET);
            params.add("code", code);
            params.add("state", state);

            HttpEntity<MultiValueMap<String, String>> naverTokenRequest = new HttpEntity<>(params, headers);

            // 토큰 발급
            ResponseEntity<String> response = restTemplate.exchange(reqUrl,
                    HttpMethod.POST,
                    naverTokenRequest,
                    String.class);

            System.out.println("Token Response: " + response.getBody());

            String responseBody = response.getBody();
            JsonObject asJsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            naverAccessToken = asJsonObject.get("access_token").getAsString();

            //프로필 정보 조회
            String profileReqUrl = "https://openapi.naver.com/v1/nid/me";

            RestTemplate profileRestTemplate = new RestTemplate();

            HttpHeaders profileHeaders = new HttpHeaders();
            profileHeaders.add("Authorization", "Bearer " + naverAccessToken);

            HttpEntity<MultiValueMap<String, String>> naverProfileRequest = new HttpEntity<>(profileHeaders);

            ResponseEntity<String> profileResponse = profileRestTemplate.exchange(profileReqUrl,
                    HttpMethod.POST,
                    naverProfileRequest,
                    String.class);

            System.out.println("Profile Response: " + profileResponse.getBody());

            String prifileResponseBody = profileResponse.getBody();
            JsonObject profileAsJsonObject = JsonParser.parseString(prifileResponseBody).getAsJsonObject();
            JsonObject profileResponseObject = profileAsJsonObject.getAsJsonObject("response");
            email = profileResponseObject.get("email").getAsString();
            email = email + "N";
            System.out.println("email: " + email);
        } catch (Exception e) {
            e.printStackTrace();
            return OAuth2ResponseDto.signInFail();
        }

        try {
            //처음 가입
            if (!userRepository.existsByUsernameAndStatus(email, 1)) {
                User user = new User(email);
                userRepository.save(user);
            }

            accessToken = jwtUtil.createJwt(email, 1000 * 60 * 15L);
            refreshToken = jwtUtil.createJwt(email, 1000 * 60 * 60 * 24 * 30L);

            long expiredTime = 1000L * 60 * 60 * 24 * 30;
            String redisKey = "RefreshToken:" + email;
            redisTemplate.opsForValue().set(redisKey, refreshToken, expiredTime, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        return OAuth2ResponseDto.success(accessToken, refreshToken);
    }
}
