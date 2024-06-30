package voicelog.voicelog.dto.response.auth;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import voicelog.voicelog.common.ResponseCode;
import voicelog.voicelog.common.ResponseMessage;
import voicelog.voicelog.dto.response.ResponseDto;

@Getter
public class RefreshAccessTokenResponseDto extends ResponseDto {

    private String newAccessToken;
    private String refreshToken;

    private RefreshAccessTokenResponseDto(String newAccessToken, String refreshToken) {
        super();
        this.newAccessToken = newAccessToken;
        this.refreshToken = refreshToken;
    }

    public static ResponseEntity<RefreshAccessTokenResponseDto> success(String newAccessToken, String refreshToken) {
        RefreshAccessTokenResponseDto responseBody = new RefreshAccessTokenResponseDto(newAccessToken, refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> refreshFail() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.INVALID_REFRESH_TOKEN, ResponseMessage.INVALID_REFRESH_TOKEN);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
    }

}
