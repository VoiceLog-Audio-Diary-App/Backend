package voicelog.voicelog.dto.request.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RefreshAccessTokenRequestDto {
    private String refreshToken;
}
