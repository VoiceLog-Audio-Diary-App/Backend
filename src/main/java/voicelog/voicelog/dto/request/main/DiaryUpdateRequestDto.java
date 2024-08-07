package voicelog.voicelog.dto.request.main;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DiaryUpdateRequestDto {
    private LocalDate date;
    private String newTitle;
    private String newContent;
}
