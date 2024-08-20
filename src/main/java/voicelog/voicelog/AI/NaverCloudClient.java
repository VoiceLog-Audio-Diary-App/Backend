package voicelog.voicelog.AI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import voicelog.voicelog.dto.response.main.MediaTextResponse;

import java.io.File;
import java.nio.file.Files;

@Component
public class NaverCloudClient {
    private final ObjectMapper objectMapper;

    @Value("${naver.cloud.id}")
    String CLIENT_ID;

    @Value("${naver.cloud.secret}")
    String CLIENT_SECRET;

    private final WebClient webClient;

    public NaverCloudClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl("https://naveropenapi.apigw.ntruss.com")
                .build();
    }

    public String soundToText(File file) {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            String language = "Kor";

            Mono<String> responseMono = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/recog/v1/stt")
                            .queryParam("lang", language)
                            .build())
                    .header("X-NCP-APIGW-API-KEY-ID", CLIENT_ID)
                    .header("X-NCP-APIGW-API-KEY", CLIENT_SECRET)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .bodyValue(fileContent)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(this::getTextFromResponse);

            return responseMono.block();
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    private String getTextFromResponse(String responseStr) {
        try {
            return objectMapper.readValue(responseStr, MediaTextResponse.class).getText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
