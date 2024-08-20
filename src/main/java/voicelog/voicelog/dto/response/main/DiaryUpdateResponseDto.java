package voicelog.voicelog.dto.response.main;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import voicelog.voicelog.common.ResponseCode;
import voicelog.voicelog.common.ResponseMessage;
import voicelog.voicelog.dto.request.main.DiaryUpdateRequestDto;
import voicelog.voicelog.dto.response.ResponseDto;

@Getter
public class DiaryUpdateResponseDto extends ResponseDto {
    private String newTitle;
    private String newContent;

    private DiaryUpdateResponseDto(String newTitle, String newContent) {
        super();
        this.newTitle = newTitle;
        this.newContent = newContent;
    }

    public static ResponseEntity<DiaryUpdateResponseDto> success(String newTitle, String newContent) {
        DiaryUpdateResponseDto responseBody = new DiaryUpdateResponseDto(newTitle, newContent);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> notExist() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.NOT_EXIST_DIARY, ResponseMessage.NOT_EXIST_DIARY);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }
}
