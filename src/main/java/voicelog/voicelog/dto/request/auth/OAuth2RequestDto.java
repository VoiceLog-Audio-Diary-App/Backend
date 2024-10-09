package voicelog.voicelog.dto.request.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OAuth2RequestDto {
    private String code;
    private String state;
}
