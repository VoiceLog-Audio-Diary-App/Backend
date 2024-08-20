package voicelog.voicelog.controller;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.eclipse.angus.mail.iap.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import voicelog.voicelog.common.ResponseCode;
import voicelog.voicelog.domain.Diary;
import voicelog.voicelog.dto.request.main.DiaryUpdateRequestDto;
import voicelog.voicelog.dto.request.main.GPTRequestDto;
import voicelog.voicelog.dto.request.main.TranscriptionRequestDto;
import voicelog.voicelog.dto.response.ResponseDto;
import voicelog.voicelog.dto.response.main.*;
import voicelog.voicelog.service.MainService;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class MainController {
    private final MainService mainService;

    //다이어리 있는지 조회
    @GetMapping("/diary")
    public ResponseEntity<? super DiaryResponseDto> getDiary(@RequestParam("date") LocalDate date) {
        String email = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            if (authentication != null) {
                email = authentication.getName();
            }
            if (email == null)
                return MainResponseDto.noAuthentication();
        } catch (Exception e) {
            e.printStackTrace();
            return MainResponseDto.databaseError();
        }

        ResponseEntity<? super DiaryResponseDto> response = mainService.getDiary(date, email);
        return response;
    }

    //stt
    @PostMapping(value = "/speech-to-text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<? super TranscriptionResponseDto> getTextByFile(
            @ModelAttribute TranscriptionRequestDto dto) {

        String email = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            if (authentication != null) {
                email = authentication.getName();
            }
            if (email == null)
                return MainResponseDto.noAuthentication();
        } catch (Exception e) {
            e.printStackTrace();
            return MainResponseDto.databaseError();
        }

        ResponseEntity<? super TranscriptionResponseDto> response = mainService.getTextByFile(dto, email);
        return response;
    }

    @PostMapping("/gpt")
    public ResponseEntity<? super GPTResponseDto> getResultByGPT(
            @RequestBody @Valid GPTRequestDto requestBody) {
        String email = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            if (authentication != null) {
                email = authentication.getName();
            }
            if (email == null)
                return MainResponseDto.noAuthentication();
        } catch (Exception e) {
            e.printStackTrace();
            return MainResponseDto.databaseError();
        }

        ResponseEntity<? super GPTResponseDto> response = mainService.getResultByGPT(requestBody, email);
        return response;
    }

    @PatchMapping("/update")
    public ResponseEntity<? super DiaryUpdateResponseDto> updateDiary(
            @RequestBody DiaryUpdateRequestDto requestBody) {
        String email = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            if (authentication != null) {
                email = authentication.getName();
            }
            if (email == null)
                return MainResponseDto.noAuthentication();
        } catch (Exception e) {
            e.printStackTrace();
            return MainResponseDto.databaseError();
        }

        ResponseEntity<? super DiaryUpdateResponseDto> response = mainService.updateDiary(requestBody, email);
        return response;
    }

    @DeleteMapping("/delete")
    public ResponseEntity<? super DiaryDeleteResponseDto> deleteDiary(@RequestParam LocalDate date) {

        String email = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            if (authentication != null) {
                email = authentication.getName();
            }
            if (email == null)
                return MainResponseDto.noAuthentication();
        } catch (Exception e) {
            e.printStackTrace();
            return MainResponseDto.databaseError();
        }

        ResponseEntity<? super DiaryDeleteResponseDto> response = mainService.deleteDiary(date, email);
        return response;
    }
}
