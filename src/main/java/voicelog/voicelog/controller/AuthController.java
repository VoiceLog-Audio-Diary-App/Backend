package voicelog.voicelog.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import voicelog.voicelog.dto.request.auth.*;
import voicelog.voicelog.dto.response.ResponseDto;
import voicelog.voicelog.dto.response.auth.*;
import voicelog.voicelog.service.AuthService;
import lombok.RequiredArgsConstructor;

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

    @PostMapping("/sign-out")
    public ResponseEntity<? super SignOutResponseDto> signOut(HttpServletRequest request) {
        String email = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            if (authentication != null) {
                email = authentication.getName();
            }
            if (email == null)
                return ResponseDto.noAuthentication();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseDto.databaseError();
        }

        //헤더에서 토큰 가져오기
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseDto.noAuthentication();
        }

        String token = authorizationHeader.substring(7);

        ResponseEntity<? super SignOutResponseDto> response = authService.signOut(token, email);
        return response;
    }
}
