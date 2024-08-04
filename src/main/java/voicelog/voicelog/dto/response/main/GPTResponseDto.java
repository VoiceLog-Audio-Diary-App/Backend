package voicelog.voicelog.dto.response.main;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import voicelog.voicelog.common.ResponseCode;
import voicelog.voicelog.common.ResponseMessage;
import voicelog.voicelog.dto.request.main.GPTRequestDto;
import voicelog.voicelog.dto.response.ResponseDto;

@Getter
public class GPTResponseDto extends ResponseDto {

    private String title;
    private String content;
    private GPTResponseDto(String title, String content) {
        super();
        this.title = title;
        this.content = content;
    }

    public static ResponseEntity<GPTResponseDto> success(String title, String content) {
        GPTResponseDto responseBody = new GPTResponseDto(title, content);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> invalidFile() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.INVALID_FILE, ResponseMessage.INVALID_FILE);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> GPTFail() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.GPT_FAIL, ResponseMessage.GPT_FAIL);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> duplicateDiary() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.DUPLICATE_DIARY, ResponseMessage.DUPLICATE_DIARY);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }
}
