package voicelog.voicelog.domain;

import jakarta.persistence.*;
import voicelog.voicelog.dto.request.auth.SignUpRequestDto;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(length = 50, nullable = false, unique = true)
    private String username;

    @Column(nullable = true)
    private String password;

    @Column(nullable = false)
    private Integer status;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime created_at;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime updated_at;

    @Column(nullable = false, length = 20)
    private String type;

    @Column
    private Integer coin;

    public User(SignUpRequestDto dto) {
        this.username = dto.getEmail();
        this.password = dto.getPassword();
        this.status = 1;
        this.created_at = LocalDateTime.now();
        this.updated_at = LocalDateTime.now();
        this.type = "voicelog";
        this.coin = 2;
    }

    public User(String username) {
        this.username = username;
        this.password = "NAVER";
        this.status = 1;
        this.created_at = LocalDateTime.now();
        this.updated_at = LocalDateTime.now();
        this.type = "naver";
        this.coin = 2;
    }
}