package voicelog.voicelog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import voicelog.voicelog.AI.NaverCloudClient;
import voicelog.voicelog.dto.request.main.TranscriptionRequestDto;
import voicelog.voicelog.dto.response.main.TranscriptionResponseDto;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MainService {
    private final NaverCloudClient naverCloudClient;

    public ResponseEntity<? super TranscriptionResponseDto> getTextByFile(TranscriptionRequestDto dto) {

        String text = "";

        try {
            MultipartFile file = dto.getFile();
            File convFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
            fos.close();

            text = naverCloudClient.soundToText(convFile);
        } catch (Exception e) {
            e.printStackTrace();
            return TranscriptionResponseDto.invalidFile();
        }
        return TranscriptionResponseDto.success(text);
    }
}
