package nekopunch_rush.testjava21.presentation.resoource.lifelog;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MealRequest {

    @NotNull
    private LocalTime time;

    @NotBlank
    private String content;

    @Min(0)
    @Max(10000)
    private int calories;

    private String photoUrl; // 任意
}

