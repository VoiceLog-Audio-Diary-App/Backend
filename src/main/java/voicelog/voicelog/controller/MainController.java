package voicelog.voicelog.controller;

import lombok.RequiredArgsConstructor;
import org.eclipse.angus.mail.iap.Response;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import voicelog.voicelog.domain.Diary;
import voicelog.voicelog.dto.request.main.TranscriptionRequestDto;
import voicelog.voicelog.dto.response.main.DiaryResponseDto;
import voicelog.voicelog.dto.response.main.TranscriptionResponseDto;
import voicelog.voicelog.service.MainService;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
public class MainController {
    private final MainService mainService;

    //다이어리 있는지 조회
    @GetMapping("/diary")
    public ResponseEntity<? super DiaryResponseDto> getDiary(@RequestParam("date") LocalDate date, @RequestParam("email") String email) {
        ResponseEntity<? super DiaryResponseDto> response = mainService.getDiary(date, email);
        return response;
    }

    @PostMapping(value = "/speech-to-text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<? super TranscriptionResponseDto> getTextByFile(
            @ModelAttribute TranscriptionRequestDto dto) {
        ResponseEntity<? super TranscriptionResponseDto> response = mainService.getTextByFile(dto);
        return response;
    }


}
