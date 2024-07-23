package voicelog.voicelog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import voicelog.voicelog.dto.request.main.TranscriptionRequestDto;
import voicelog.voicelog.dto.response.main.TranscriptionResponseDto;
import voicelog.voicelog.service.MainService;

@RestController
@RequiredArgsConstructor
public class MainController {
    private final MainService mainService;

    @PostMapping(value = "/speech-to-text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<? super TranscriptionResponseDto> getTextByFile(
            @ModelAttribute TranscriptionRequestDto dto) {
        ResponseEntity<? super TranscriptionResponseDto> response = mainService.getTextByFile(dto);
        return response;
    }
}
