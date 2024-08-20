package voicelog.voicelog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableFeignClients
public class VoicelogApplication {

	public static void main(String[] args) {
		SpringApplication.run(VoicelogApplication.class, args);
	}

}
