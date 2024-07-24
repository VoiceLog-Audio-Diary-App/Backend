package voicelog.voicelog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import voicelog.voicelog.AI.NaverCloudClient;
import voicelog.voicelog.domain.Diary;
import voicelog.voicelog.domain.User;
import voicelog.voicelog.dto.request.main.TranscriptionRequestDto;
import voicelog.voicelog.dto.response.main.DiaryResponseDto;
import voicelog.voicelog.dto.response.main.TranscriptionResponseDto;
import voicelog.voicelog.repository.DiaryRepository;
import voicelog.voicelog.repository.UserRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MainService {

    private final NaverCloudClient naverCloudClient;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    @Value("${openai.api.key}")
    private String apiKey;

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

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

    public ResponseEntity<? super DiaryResponseDto> getDiary(LocalDate date, String email) {
        User user = userRepository.findByUsername(email);
        String content;
        try {
            Optional<Diary> optionalDiary = diaryRepository.findByDateAndUser(date, user);
            if (optionalDiary.isPresent())
            {
                Diary diary = optionalDiary.get();
                content = diary.getContent();
            }
            else
                content = null;
        } catch (Exception e) {
            e.printStackTrace();
            return DiaryResponseDto.databaseError();
        }
        return DiaryResponseDto.success(content);
    }
}
