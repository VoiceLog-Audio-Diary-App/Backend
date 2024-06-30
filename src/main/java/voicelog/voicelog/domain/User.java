package voicelog.voicelog.domain;

import jakarta.persistence.*;
import voicelog.voicelog.dto.request.SignUpRequestDto;
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

    @Column(nullable = false)
    private String password;
/*
    @Column(nullable = false)
    private String name;*/

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

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private RefreshToken refreshToken;

    public User(SignUpRequestDto dto) {
        this.username = dto.getEmail();
        this.password = dto.getPassword();
        this.status = 1;
        this.created_at = LocalDateTime.now();
        this.updated_at = LocalDateTime.now();
        this.type = "voicelog";
    }
}