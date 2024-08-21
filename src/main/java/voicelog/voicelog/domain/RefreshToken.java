package voicelog.voicelog.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@RedisHash(value = "RefreshToken", timeToLive = 2592000)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    private Long id;

    @Indexed
    private Long userId;

    @Indexed
    private String refreshToken;

    private LocalDateTime expiredDate;

    private LocalDateTime createdDate;
}
