package voicelog.voicelog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import voicelog.voicelog.domain.CustomOAuth2User;
import voicelog.voicelog.domain.RefreshToken;
import voicelog.voicelog.domain.User;
import voicelog.voicelog.repository.RefreshTokenRepository;
import voicelog.voicelog.repository.UserRepository;
import voicelog.voicelog.utils.JwtUtil;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);
        String oauthClientName = request.getClientRegistration().getClientName();

        User user = null;
        String userEmail = null;

        try {
            log.info(new ObjectMapper().writeValueAsString(oAuth2User.getAttributes()));

            if (oauthClientName.equals("naver")) {
                Map<String, String> responseMap = (Map<String, String>) oAuth2User.getAttributes().get("response");
                userEmail = responseMap.get("email");

                if (userEmail == null) {
                    throw new OAuth2AuthenticationException("Email not found in OAuth2 response");
                }

                user = new User(userEmail);
            }

            String refreshToken = jwtUtil.createJwt(userEmail, 1000 * 60 * 60 * 24 * 30L);

            if (userRepository.existsByUsername(userEmail)){
                User existUser = userRepository.findByUsername(userEmail);
                Optional<RefreshToken> optionalToken = refreshTokenRepository.findByUser(existUser);

                if (existUser.getType() == "naver")
                {
                    log.info("User with email {} signed in successfully.", userEmail);

                    RefreshToken refreshToken1 = optionalToken.get();

                    refreshToken1.setRefreshToken(refreshToken);
                    refreshToken1.setExpiredDate(LocalDateTime.now().plus(1000 * 60 * 60 * 24 * 30L, ChronoUnit.MILLIS));
                    refreshToken1.setCreatedDate(LocalDateTime.now());

                    refreshTokenRepository.save(refreshToken1);
                }
                else//자체 로그인 유저 있는 경우
                {
                    existUser.setType("naver");
                    existUser.setUpdated_at(LocalDateTime.now());
                    userRepository.save(existUser);

                    if (optionalToken.isPresent()) {
                        RefreshToken refreshToken1 = optionalToken.get();

                        refreshToken1.setRefreshToken(refreshToken);
                        refreshToken1.setExpiredDate(LocalDateTime.now().plus(1000 * 60 * 60 * 24 * 30L, ChronoUnit.MILLIS));
                        refreshToken1.setCreatedDate(LocalDateTime.now());

                        refreshTokenRepository.save(refreshToken1);
                    } else {
                        RefreshToken refreshToken1 = new RefreshToken();

                        refreshToken1.setUser(existUser);
                        refreshToken1.setRefreshToken(refreshToken);
                        refreshToken1.setExpiredDate(LocalDateTime.now().plus(1000 * 60 * 60 * 24 * 30L, ChronoUnit.MILLIS));
                        refreshToken1.setCreatedDate(LocalDateTime.now());

                        refreshTokenRepository.save(refreshToken1);
                    }
                }
            } else {//네이버 회원가입
                userRepository.save(user);

                RefreshToken refreshToken1 = new RefreshToken();
                refreshToken1.setUser(user);
                refreshToken1.setRefreshToken(refreshToken);
                refreshToken1.setExpiredDate(LocalDateTime.now().plus(1000 * 60 * 60 * 24 * 30L, ChronoUnit.MILLIS));
                refreshToken1.setCreatedDate(LocalDateTime.now());

                refreshTokenRepository.save(refreshToken1);
            }



        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return new CustomOAuth2User(userEmail);
    }
}
