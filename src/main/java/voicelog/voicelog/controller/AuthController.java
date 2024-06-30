package voicelog.voicelog.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import voicelog.voicelog.dto.request.*;
import voicelog.voicelog.dto.response.auth.*;
import voicelog.voicelog.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/email-check")
    public ResponseEntity<? super EmailCheckResponseDto> emailCheck(
            @RequestBody @Valid EmailCheckRequestDto requestBody) {
        ResponseEntity<? super EmailCheckResponseDto> response = authService.emailCheck(requestBody);
        return response;
    }

    @PostMapping("/email-certification")
    public ResponseEntity<? super EmailCertificationResponseDto> emailCertification(
            @RequestBody @Valid EmailCertificationRequestDto requestBody) {
        ResponseEntity<? super EmailCertificationResponseDto> response = authService.emailCertification(requestBody);
        return response;
    }

    @PostMapping("/certification-check")
    public ResponseEntity<? super CertificationCheckResponseDto> certificationCheck(
            @RequestBody @Valid CertificationCheckRequestDto requestBody) {
        ResponseEntity<? super CertificationCheckResponseDto> response = authService.certificationCheck(requestBody);
        return response;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<? super SignUpResponseDto> signUp(
            @RequestBody @Valid SignUpRequestDto requestBody) {
        ResponseEntity<? super SignUpResponseDto> response = authService.signUp(requestBody);
        return response;
    }

    @PostMapping("/sign-in")
    public ResponseEntity<? super SignInResponseDto> signUp(
            @RequestBody @Valid SignInRequestDto requestBody) {
        ResponseEntity<? super SignInResponseDto> response = authService.signIn(requestBody);
        return response;
    }

    @PostMapping("/refreshAccessToken")
    public ResponseEntity<? super RefreshAccessTokenResponseDto> refreshAccessToken(
            @RequestBody @Valid RefreshAccessTokenRequestDto requestBody) {
        ResponseEntity<? super RefreshAccessTokenResponseDto> response = authService.refreshAccessToken(requestBody);
        return response;
    }
}
