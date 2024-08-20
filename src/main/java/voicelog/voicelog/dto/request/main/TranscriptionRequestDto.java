package voicelog.voicelog.dto.request.main;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class TranscriptionRequestDto {
    private MultipartFile file;
}
