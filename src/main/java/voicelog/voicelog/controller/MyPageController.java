package voicelog.voicelog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import voicelog.voicelog.dto.request.auth.PasswordCheckRequestDto;
import voicelog.voicelog.dto.request.auth.PasswordPatchRequestDto;
import voicelog.voicelog.dto.response.ResponseDto;
import voicelog.voicelog.dto.response.auth.PasswordCheckResponseDto;
import voicelog.voicelog.dto.response.auth.PasswordPatchResponseDto;
import voicelog.voicelog.dto.response.auth.SocialUserCheckResponseDto;
import voicelog.voicelog.service.MyPageService;

@RestController
@RequiredArgsConstructor
public class MyPageController {
    private final MyPageService myPageService;

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

        ResponseEntity<? super SocialUserCheckResponseDto> response = myPageService.socialCheck(email);
        return response;
    }

    @PostMapping("/password-check")
    public ResponseEntity<? super PasswordCheckResponseDto> passwordCheck(
            @RequestBody @Valid PasswordCheckRequestDto requestBody) {
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

        ResponseEntity<? super PasswordCheckResponseDto> response = myPageService.oldPasswordCheck(requestBody, email);
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

        ResponseEntity<? super PasswordPatchResponseDto> response = myPageService.passwordPatch(requestBody, email);
        return response;
    }
}
