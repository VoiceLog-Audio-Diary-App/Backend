package voicelog.voicelog.dto.response.auth;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import voicelog.voicelog.common.ResponseCode;
import voicelog.voicelog.common.ResponseMessage;
import voicelog.voicelog.dto.response.ResponseDto;

@Getter
public class PasswordPatchResponseDto extends ResponseDto {
    private PasswordPatchResponseDto() {super();}

    public static ResponseEntity<PasswordPatchResponseDto> success() {
        PasswordPatchResponseDto responseBody = new PasswordPatchResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> notEqualPassword() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.NOT_INVALID_PASSWORD, ResponseMessage.NOT_INVALID_PASSWORD);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> reusedPassword() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.PASSWORD_REUSE, ResponseMessage.PASSWORD_REUSE);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }
}
