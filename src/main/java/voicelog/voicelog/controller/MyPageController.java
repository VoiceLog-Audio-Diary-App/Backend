package voicelog.voicelog.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import voicelog.voicelog.dto.request.mypage.PasswordCheckRequestDto;
import voicelog.voicelog.dto.request.mypage.PasswordPatchRequestDto;
import voicelog.voicelog.dto.response.ResponseDto;
import voicelog.voicelog.dto.response.auth.SignOutResponseDto;
import voicelog.voicelog.dto.response.mypage.DeleteUserResponseDto;
import voicelog.voicelog.dto.response.mypage.PasswordCheckResponseDto;
import voicelog.voicelog.dto.response.mypage.PasswordPatchResponseDto;
import voicelog.voicelog.dto.response.mypage.SocialUserCheckResponseDto;
import voicelog.voicelog.service.MyPageService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mypage")
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

    @DeleteMapping("/delete")
    public ResponseEntity<? super DeleteUserResponseDto> deleteUser(HttpServletRequest request) {
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

        ResponseEntity<? super DeleteUserResponseDto> response = myPageService.deleteUser(token, email);
        return response;
    }
}
