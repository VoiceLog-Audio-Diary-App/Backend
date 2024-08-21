package voicelog.voicelog.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import voicelog.voicelog.config.RedisTemplateConfig;
import voicelog.voicelog.service.AuthService;
import voicelog.voicelog.utils.JwtUtil;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "Blacklist:";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if ("/login/oauth2/code/kakao".equals(requestURI)) {
            log.info("Skipping JWT filter for /login/oauth2/code/kakao");
            filterChain.doFilter(request, response);
            return;
        }

        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        log.info("authorization : {}", authorization);

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.error("authorization 없거나 형식  불일치.");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 토큰 추출
            String token = authorization.split(" ")[1];
            log.info("Parsed token : {}", token);

            //블랙리스트에 있는 지 확인
            String redisKey = BLACKLIST_PREFIX + token;
            Boolean isBlacklisted = redisTemplate.hasKey(redisKey);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                log.error("Token is blacklisted");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is blacklisted.");
                return;
            }

            // 토큰 만료 여부 확인
            if (JwtUtil.isExpired(token)) {
                log.error("Token is expired");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is expired.");
                return;
            }

            // 토큰에서 username 추출
            String username = JwtUtil.getUsername(token);
            log.info("Extracted Username : {}", username);

            // 토큰 검증
            if (!JwtUtil.validateToken(token, username)) {
                log.error("Invalid token");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token.");
                return;
            }

            // 권한 부여
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority("USER")));

            //detail 넣어주기
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        } catch (ExpiredJwtException e) { // 토큰 만료 예외 처리
            log.error("Token expired exception: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is expired.");
            return;
        } catch (JwtException e) { // JWT 예외 처리
            log.error("JWT exception: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token.");
            return;
        } catch (Exception e) { // 기타 예외 처리
            log.error("Unexpected error occurred while processing the JWT: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
