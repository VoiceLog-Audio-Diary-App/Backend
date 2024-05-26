package voicelog.voicelog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResponseDto {
    private String responseCode;
    private String responseMessage;

    public ResponseDto() {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }
}
