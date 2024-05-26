package voicelog.voicelog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class VoicelogApplication {

	public static void main(String[] args) {
		SpringApplication.run(VoicelogApplication.class, args);
	}

}
