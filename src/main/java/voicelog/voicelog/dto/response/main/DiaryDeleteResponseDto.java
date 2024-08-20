package voicelog.voicelog.dto.response.main;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import voicelog.voicelog.common.ResponseCode;
import voicelog.voicelog.common.ResponseMessage;
import voicelog.voicelog.dto.response.ResponseDto;

@Getter
public class DiaryDeleteResponseDto extends ResponseDto {
    public DiaryDeleteResponseDto(){super();};

    public static ResponseEntity<DiaryDeleteResponseDto> success() {
        DiaryDeleteResponseDto responseBody = new DiaryDeleteResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> notExist() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.NOT_EXIST_DIARY, ResponseMessage.NOT_EXIST_DIARY);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
    }
}
