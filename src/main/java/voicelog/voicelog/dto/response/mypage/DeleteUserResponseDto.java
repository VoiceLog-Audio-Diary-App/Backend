package voicelog.voicelog.dto.response.mypage;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import voicelog.voicelog.common.ResponseCode;
import voicelog.voicelog.common.ResponseMessage;
import voicelog.voicelog.dto.response.ResponseDto;
import voicelog.voicelog.dto.response.auth.SignOutResponseDto;

@Getter
public class DeleteUserResponseDto extends ResponseDto{
    public static ResponseEntity<DeleteUserResponseDto> success() {
        DeleteUserResponseDto responseBody = new DeleteUserResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> notExistUser() {
        ResponseDto result = new ResponseDto(ResponseCode.NOT_EXIST_USER, ResponseMessage.NOT_EXIST_USER);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    }
}
