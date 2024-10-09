package voicelog.voicelog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import voicelog.voicelog.domain.CustomOAuth2User;
import voicelog.voicelog.domain.User;
import voicelog.voicelog.repository.UserRepository;
import voicelog.voicelog.utils.JwtUtil;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {
//    private final UserRepository userRepository;
//    private final JwtUtil jwtUtil;
//    private final RedisTemplate<String, String> redisTemplate;
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
//        OAuth2User oAuth2User = super.loadUser(request);
//        String oauthClientName = request.getClientRegistration().getClientName();
//
//        User user = null;
//        String userEmail = null;
//
//        try {
//            log.info(new ObjectMapper().writeValueAsString(oAuth2User.getAttributes()));
//
//            if (oauthClientName.equals("naver")) {
//                Map<String, String> responseMap = (Map<String, String>) oAuth2User.getAttributes().get("response");
//                userEmail = responseMap.get("email");
//
//                if (userEmail == null) {
//                    throw new OAuth2AuthenticationException("Email not found in OAuth2 response");
//                }
//
//                userEmail = userEmail + 'N';
//                user = new User(userEmail);
//                userRepository.save(user);
//            }
//
//            String refreshToken = jwtUtil.createJwt(userEmail, 1000 * 60 * 60 * 24 * 30L);
//
//            long expiredTime = 1000L * 60 * 60 * 24 * 30;
//            String redisKey2 = "RefreshToken:" + userEmail;
//            redisTemplate.opsForValue().set(redisKey2, refreshToken, expiredTime, TimeUnit.MILLISECONDS);
//
//            log.info("User with email {} signed in successfully.", userEmail);
//
//        } catch (Exception exception) {
//            exception.printStackTrace();
//        }
//        return new CustomOAuth2User(userEmail);
//    }
}
