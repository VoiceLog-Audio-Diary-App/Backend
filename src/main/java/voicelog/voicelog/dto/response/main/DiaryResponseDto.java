package voicelog.voicelog.dto.response.main;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import voicelog.voicelog.common.ResponseCode;
import voicelog.voicelog.common.ResponseMessage;
import voicelog.voicelog.dto.response.ResponseDto;

@Getter
public class DiaryResponseDto extends ResponseDto {

    private String content;
    private String title;
    private DiaryResponseDto(String title, String content) {
        super();
        this.content = content;
        this.title = title;
    }

    public static ResponseEntity<DiaryResponseDto> success(String title, String content) {
        DiaryResponseDto responseBody = new DiaryResponseDto(title, content);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> databaseError() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.DATABASE_ERROR, ResponseMessage.DATABASE_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseBody);
    }
}
