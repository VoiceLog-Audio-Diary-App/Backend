package voicelog.voicelog.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import voicelog.voicelog.dto.request.EmailRequestDto;
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
    private String certificationNumber;

    @PostMapping("/sign-up")
    public ResponseEntity<ResponseDto> signUp(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
        ResponseDto response = authService.signUp(signUpRequestDto);
        if (ResponseCode.SUCCESS.equals(response.getResponseCode())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/mailSend")
    public HashMap<String, Object> mailSend(@Valid @RequestBody EmailRequestDto emailDto) {
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
    }
}
