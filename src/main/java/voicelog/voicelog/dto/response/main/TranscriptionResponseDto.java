package voicelog.voicelog.dto.response.main;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import voicelog.voicelog.common.ResponseCode;
import voicelog.voicelog.common.ResponseMessage;
import voicelog.voicelog.dto.response.ResponseDto;

@Getter
public class TranscriptionResponseDto extends ResponseDto {

    private String text;
    private TranscriptionResponseDto(String text) {
        super();
        this.text = text;
    }

    public static ResponseEntity<TranscriptionResponseDto> success(String text) {
        TranscriptionResponseDto responseBody = new TranscriptionResponseDto(text);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> invalidFile() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.INVALID_FILE, ResponseMessage.INVALID_FILE);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> sttFail() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.STT_FAIL, ResponseMessage.STT_FAIL);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
    }
}
