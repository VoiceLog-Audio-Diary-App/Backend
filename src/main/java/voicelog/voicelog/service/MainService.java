package voicelog.voicelog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import voicelog.voicelog.AI.GPTClient;
import voicelog.voicelog.AI.NaverCloudClient;
import voicelog.voicelog.domain.Diary;
import voicelog.voicelog.domain.User;
import voicelog.voicelog.dto.request.main.DiaryUpdateRequestDto;
import voicelog.voicelog.dto.request.main.GPTRequestDto;
import voicelog.voicelog.dto.request.main.TranscriptionRequestDto;
import voicelog.voicelog.dto.response.main.DiaryResponseDto;
import voicelog.voicelog.dto.response.main.DiaryUpdateResponseDto;
import voicelog.voicelog.dto.response.main.GPTResponseDto;
import voicelog.voicelog.dto.response.main.TranscriptionResponseDto;
import voicelog.voicelog.repository.DiaryRepository;
import voicelog.voicelog.repository.UserRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MainService {

    private final NaverCloudClient naverCloudClient;
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final GPTClient gptClient;

    private String threadId;

    private String runId;

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
        String content = null;
        String title = null;
        try {
            Optional<Diary> optionalDiary = diaryRepository.findByDateAndUserAndDeleted(date, user, false);
            if (optionalDiary.isPresent())
            {
                Diary diary = optionalDiary.get();
                content = diary.getContent();
                title = diary.getTitle();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DiaryResponseDto.databaseError();
        }
        return DiaryResponseDto.success(title, content);
    }

    public ResponseEntity<? super GPTResponseDto> getResultByGPT(GPTRequestDto dto, String email){

        String title = "", content = "";

        User user = userRepository.findByUsername(email);

        if (dto.getInput() == null)
            return GPTResponseDto.invalidFile();

        //같은 날짜의 일기가 있을 경우
        try {
            Optional<Diary> optionalDiary = diaryRepository.findByDateAndUserAndDeleted(dto.getDate(), user, false);
            if (optionalDiary.isPresent())
            {
                return GPTResponseDto.duplicateDiary();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DiaryResponseDto.databaseError();
        }

        //thread 생성
        threadId = gptClient.createThread();
        if (threadId == null)
            return GPTResponseDto.GPTFail();

        //messages 생성
        if (gptClient.createMessage(dto.getInput(), threadId) != 200)
            return GPTResponseDto.GPTFail();

        //run 생성
        runId = gptClient.createRun(threadId);
        if (runId == null)
            return GPTResponseDto.GPTFail();

        //run 완료되도록 대기
        try {
            Thread.sleep(5000); // 5000밀리초(5초) 동안 대기
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //run 찾기
        if (gptClient.retrieveRun(threadId, runId) != 200)
            return GPTResponseDto.GPTFail();

        //메시지 가져오기
        String[] result = gptClient.listMessages(threadId);
        if (result == null)
            return GPTResponseDto.GPTFail();
        else
        {
            title = result[0];
            content = result[1];

            //DB 저장
            try {
                Diary diary = new Diary();
                diary.setTitle(title);
                diary.setContent(content);
                diary.setDate(dto.getDate());
                diary.setUser(user);
                diary.setCreatedAt(LocalDateTime.now());
                diary.setUpdatedAt(LocalDateTime.now());

                diaryRepository.save(diary);
            } catch (Exception e) {
                e.printStackTrace();
                return GPTResponseDto.databaseError();
            }
        }

        //thread 삭제
        if (gptClient.deleteThread(threadId) != 200)
            return GPTResponseDto.GPTFail();

        return GPTResponseDto.success(title, content);
    }

    public ResponseEntity<? super DiaryUpdateResponseDto> updateDiary(DiaryUpdateRequestDto dto, String email) {

        User user = userRepository.findByUsername(email);

        Optional<Diary> optionalDiary = diaryRepository.findByDateAndUserAndDeleted(dto.getDate(), user, false);

        try {
            if (optionalDiary.isPresent()) {
                Diary diary = optionalDiary.get();
                diary.setTitle(dto.getNewTitle());
                diary.setContent(dto.getNewContent());
                diary.setUpdatedAt(LocalDateTime.now());
                diaryRepository.save(diary);
            } else {
                return DiaryUpdateResponseDto.notExist();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DiaryUpdateResponseDto.databaseError();
        }

        return DiaryUpdateResponseDto.success(dto.getNewTitle(), dto.getNewContent());
    }
}
