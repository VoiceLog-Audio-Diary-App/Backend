package voicelog.voicelog.dto.request.main;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class GPTRequestDto {
    private String input;
    private LocalDate date;
}
