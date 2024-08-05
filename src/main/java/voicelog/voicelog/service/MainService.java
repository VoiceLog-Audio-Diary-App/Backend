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
import voicelog.voicelog.dto.request.main.GPTRequestDto;
import voicelog.voicelog.dto.request.main.TranscriptionRequestDto;
import voicelog.voicelog.dto.response.main.DiaryResponseDto;
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

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.assistant.id}")
    private String assistantId;

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

    public ResponseEntity<? super GPTResponseDto> getResultByGPT(GPTRequestDto dto, String email){

        String title = "", content = "";

        User user = userRepository.findByUsername(email);

        if (dto.getInput() == null)
            return GPTResponseDto.invalidFile();

        //같은 날짜의 일기가 있을 경우
        try {
            Optional<Diary> optionalDiary = diaryRepository.findByDateAndUser(dto.getDate(), user);
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
            Thread.sleep(4000); // 5000밀리초(5초) 동안 대기
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

        /*try{
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            String url = "https://api.openai.com/v1/threads/" + threadId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("OpenAI-Beta", "assistants=v2")
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("thread 삭제 응답 코드: " + response.statusCode());
            System.out.println("thread 삭제 응답 본문: " + response.body());

        } catch (Exception e) {
            e.printStackTrace();
            return GPTResponseDto.GPTFail();
        }*/

        return GPTResponseDto.success(title, content);
    }
}
