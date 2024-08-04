package voicelog.voicelog.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
        try{
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            String url = "https://api.openai.com/v1/threads/";

            String json = "{}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("OpenAI-Beta", "assistants=v2")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("thread 생성 응답 코드: " + response.statusCode());
            System.out.println("thread 생성 응답 본문: " + response.body());

            String responseBody = response.body();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // thread id 추출
            threadId = rootNode.path("id").asText();
            System.out.println("Thread ID: " + threadId);

        } catch (Exception e) {
            e.printStackTrace();
            return GPTResponseDto.GPTFail();
        }

        //messages 생성
        try{
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            String url = "https://api.openai.com/v1/threads/" + threadId + "/messages";

            String json = "{"
                    + "\"role\": \"user\","
                    + "\"content\": \"" + dto.getInput() + "\""
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("OpenAI-Beta", "assistants=v2")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("응답 코드: " + response.statusCode());
            System.out.println("응답 본문: " + response.body());

        } catch (Exception e) {
            e.printStackTrace();
            return GPTResponseDto.GPTFail();
        }

        //run 생성
        try {
            HttpClient client = HttpClient.newHttpClient();

            String url = "https://api.openai.com/v1/threads/" + threadId + "/runs";

            String json = "{"
                    + "\"assistant_id\": \"" + assistantId + "\""
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("OpenAI-Beta", "assistants=v2")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> runResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = runResponse.body();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // "id" 필드 값 추출
            runId = rootNode.path("id").asText();
            System.out.println("Run ID: " + runId);

            System.out.println("run 응답 코드: " + runResponse.statusCode());
            System.out.println("run 응답 본문: " + runResponse.body());

        } catch (Exception e) {
            e.printStackTrace();
            return GPTResponseDto.GPTFail();
        }

        //run 완료되도록 대기
        try {
            Thread.sleep(5000); // 5000밀리초(5초) 동안 대기
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //run 찾기
        try {
            HttpClient client = HttpClient.newHttpClient();

            String url = "https://api.openai.com/v1/threads/" + threadId + "/runs/" + runId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    //.header("Content-Type", "application/json")
                    .header("OpenAI-Beta", "assistants=v2")
                    .GET()
                    .build();

            HttpResponse<String> runResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("retrieve 응답 코드: " + runResponse.statusCode());
            System.out.println("retrieve 응답 본문: " + runResponse.body());

        } catch (Exception e) {
            e.printStackTrace();
            return GPTResponseDto.GPTFail();
        }

        //메시지 가져오기
        try {
            HttpClient client = HttpClient.newHttpClient();

            String url = "https://api.openai.com/v1/threads/" + threadId + "/messages";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("OpenAI-Beta", "assistants=v2")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("list 응답 코드: " + response.statusCode());
            System.out.println("list 응답 본문: " + response.body());

            String responseBody = response.body();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // "data" 배열의 첫 번째 객체 가져오기
            JsonNode firstMessageNode = rootNode.path("data").get(0);

            // "content" 배열의 첫 번째 객체 가져오기
            JsonNode contentNode = firstMessageNode.path("content").get(0);

            // "value" 필드에서 JSON 형식의 문자열 추출
            String contentValue = contentNode.path("text").path("value").asText();

            // JSON 형식의 문자열을 다시 파싱하여 title과 content 추출
            JsonNode parsedContent = objectMapper.readTree(contentValue);
            title = parsedContent.path("output").path("제목").asText();
            content = parsedContent.path("output").path("일기 내용").asText();

            // 결과 출력
            System.out.println("Title: " + title);
            System.out.println("Content: " + content);

        } catch (Exception e) {
            e.printStackTrace();
            return GPTResponseDto.GPTFail();
        }

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

        //thread 삭제
        try{
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
        }

        return GPTResponseDto.success(title, content);
    }
}
