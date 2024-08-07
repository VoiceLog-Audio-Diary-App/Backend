package voicelog.voicelog.AI;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import voicelog.voicelog.dto.response.main.GPTResponseDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class GPTClient {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.assistant.id}")
    private String assistantId;

    //private String threadId;

    //private String runId;

    public String createThread() {
        String threadId = null;
        try{
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            String url = "https://api.openai.com/v1/threads";

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
            return null;
        }

        return threadId;
    }

    public int createMessage(String input, String threadId) {
        int result = 0;
        try{
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_2)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            String url = "https://api.openai.com/v1/threads/" + threadId + "/messages";

            String json = "{"
                    + "\"role\": \"user\","
                    + "\"content\": \"" + input + "\""
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("OpenAI-Beta", "assistants=v2")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            result = response.statusCode();
            System.out.println("message 생성 응답 코드: " + response.statusCode());
            System.out.println("message 생성 본문: " + response.body());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public String createRun(String threadId){
        String runId;
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
            return null;
        }

        return runId;
    }

    public int retrieveRun(String threadId, String runId) {
        int result = 0;
        try {
            HttpClient client = HttpClient.newHttpClient();

            String url = "https://api.openai.com/v1/threads/" + threadId + "/runs/" + runId;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("OpenAI-Beta", "assistants=v2")
                    .GET()
                    .build();

            HttpResponse<String> runResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

            result = runResponse.statusCode();
            System.out.println("retrieve 응답 코드: " + runResponse.statusCode());
            System.out.println("retrieve 응답 본문: " + runResponse.body());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public String[] listMessages(String threadId) {
        String title, content;
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
            return null;
        }

        return new String[]{title, content};
    }

    public int deleteThread(String threadId) {
        int result = 0;
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

            result = response.statusCode();
            System.out.println("thread 삭제 응답 코드: " + response.statusCode());
            System.out.println("thread 삭제 응답 본문: " + response.body());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
