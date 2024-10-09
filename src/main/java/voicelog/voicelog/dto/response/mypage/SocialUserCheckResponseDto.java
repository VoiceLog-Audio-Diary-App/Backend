package voicelog.voicelog.dto.response.mypage;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import voicelog.voicelog.common.ResponseCode;
import voicelog.voicelog.common.ResponseMessage;
import voicelog.voicelog.dto.response.ResponseDto;

@Getter
public class SocialUserCheckResponseDto extends ResponseDto {

    private SocialUserCheckResponseDto() {super();}

    public static ResponseEntity<SocialUserCheckResponseDto> success() {
        SocialUserCheckResponseDto responseBody = new SocialUserCheckResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> socialRequest() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.NO_PERMISSION, ResponseMessage.NO_PERMISSION);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseBody);
    }
}
