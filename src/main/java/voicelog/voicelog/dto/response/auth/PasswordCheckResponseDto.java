package voicelog.voicelog.dto.response.auth;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import voicelog.voicelog.common.ResponseCode;
import voicelog.voicelog.common.ResponseMessage;
import voicelog.voicelog.dto.response.ResponseDto;

@Getter
public class PasswordCheckResponseDto extends ResponseDto {
    private PasswordCheckResponseDto() {super();}

    public static ResponseEntity<PasswordCheckResponseDto> success() {
        PasswordCheckResponseDto responseBody = new PasswordCheckResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> wrongPassword() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.NOT_INVALID_PASSWORD, ResponseMessage.NOT_INVALID_PASSWORD);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }
}
