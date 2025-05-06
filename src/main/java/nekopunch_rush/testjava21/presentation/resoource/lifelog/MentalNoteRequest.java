package nekopunch_rush.testjava21.presentation.resoource.lifelog;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MentalNoteRequest {

    @NotNull
    private LocalDateTime recordedAt;

    @Min(0)
    @Max(10)
    private int stressLevel;

    @Min(0)
    @Max(10)
    private int motivationLevel;

    private String note; // 任意
}
