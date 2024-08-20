package voicelog.voicelog.controller;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import voicelog.voicelog.dto.request.auth.*;
import voicelog.voicelog.dto.response.ResponseDto;
import voicelog.voicelog.dto.response.auth.*;
import voicelog.voicelog.dto.response.main.MainResponseDto;
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

    @GetMapping("/social-check")
    public ResponseEntity<? super SocialUserCheckResponseDto> socialCheck() {

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

        ResponseEntity<? super SocialUserCheckResponseDto> response = authService.socialCheck(email);
        return response;
    }

    @PostMapping("/old-password-check")
    public ResponseEntity<? super OldPasswordCheckResponseDto> oldPasswordCheck(
            @RequestBody @Valid OldPasswordCheckRequestDto requestBody) {
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

        ResponseEntity<? super OldPasswordCheckResponseDto> response = authService.oldPasswordCheck(requestBody, email);
        return response;
    }

    @PatchMapping("/password-patch")
    public ResponseEntity<? super PasswordPatchResponseDto> passwordPatch(
            @RequestBody @Valid PasswordPatchRequestDto requestBody) {
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

        ResponseEntity<? super PasswordPatchResponseDto> response = authService.passwordPatch(requestBody, email);
        return response;
    }
}
