package voicelog.voicelog.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import voicelog.voicelog.dto.request.CertificationCheckRequestDto;
import voicelog.voicelog.dto.request.EmailCertificationRequestDto;
import voicelog.voicelog.dto.request.EmailCheckRequestDto;
import voicelog.voicelog.dto.response.CertificationCheckResponseDto;
import voicelog.voicelog.dto.response.EmailCertificationResponseDto;
import voicelog.voicelog.dto.response.EmailCheckResponseDto;
import voicelog.voicelog.service.AuthService;
import voicelog.voicelog.common.ResponseCode;
import voicelog.voicelog.dto.request.SignUpRequestDto;
import voicelog.voicelog.dto.response.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
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
    public ResponseEntity<ResponseDto> signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        ResponseDto response = authService.signUp(signUpRequestDto);
        if (ResponseCode.SUCCESS.equals(response.getResponseCode())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }



    /*@PostMapping("/mailSend")
    public HashMap<String, Object> mailSend(@Valid @RequestBody EmailCertificationRequestDto emailDto) {
        HashMap<String, Object> map = new HashMap<>();

        try {
            authService.sendMail(emailDto);

            map.put("success", Boolean.TRUE);
        } catch (Exception e) {
            map.put("success", Boolean.FALSE);
            map.put("error", e.getMessage());
        }

        return map;
    }

    @GetMapping("/mailCheck")
    public ResponseEntity<?> mailCheck(@RequestParam String mail, @RequestParam String userNumber) {

        boolean isMatch = authService.checkVerificationNumber(mail, userNumber);
        return ResponseEntity.ok(isMatch);
    }*/


}
