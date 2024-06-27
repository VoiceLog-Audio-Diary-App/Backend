package voicelog.voicelog.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${JWT_SECRET}")
    private String JWT_SECRET;
    private static SecretKey key;

    @PostConstruct
    public void init() {
        if (JWT_SECRET != null) {
            byte[] keyBytes = Decoders.BASE64.decode(JWT_SECRET);
            this.key = Keys.hmacShaKeyFor(keyBytes);
        } else {
            throw new IllegalStateException("JWT_SECRET is not set");
        }
    }
    private static SecretKey getSigningKey() {
        if (key == null) {
            throw new IllegalStateException("Signing key is not initialized");
        }
        return key;
    }

    public String createJwt(String username, Long expiredMs) {

        //Map<String, Object> claims = new HashMap<>();
        //claims.put("username", username);

        return Jwts.builder()
                .claim("username", username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static boolean isExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }

    public static String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("username", String.class);
    }
}
