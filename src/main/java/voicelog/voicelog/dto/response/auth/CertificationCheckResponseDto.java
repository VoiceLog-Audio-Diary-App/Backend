package voicelog.voicelog.dto.response.auth;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import voicelog.voicelog.common.ResponseCode;
import voicelog.voicelog.common.ResponseMessage;
import voicelog.voicelog.dto.response.ResponseDto;

@Getter
public class CertificationCheckResponseDto extends ResponseDto {
    private CertificationCheckResponseDto() {super();}

    public static ResponseEntity<CertificationCheckResponseDto> success() {
        CertificationCheckResponseDto responseBody = new CertificationCheckResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> certificationFail() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.CERTIFICATE_FAIL, ResponseMessage.CERTIFICATE_FAIL);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
    }
}
