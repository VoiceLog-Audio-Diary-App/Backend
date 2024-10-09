package voicelog.voicelog.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import voicelog.voicelog.domain.CustomOAuth2User;
import voicelog.voicelog.utils.JwtUtil;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//
//        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
//
//        String userEmail = oAuth2User.getName() + 'N';
//        String accessToken = jwtUtil .createJwt(userEmail, 1000 * 60 * 15L);
//
//        response.sendRedirect("http://localhost:8080/login/oauth2/code/naver/"+ accessToken);

    }
}
