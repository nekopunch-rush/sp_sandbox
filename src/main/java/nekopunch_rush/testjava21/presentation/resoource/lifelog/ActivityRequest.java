package nekopunch_rush.testjava21.presentation.resoource.lifelog;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.validation.ValidCaloriesBurned;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.validation.ValidSteps;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ActivityRequest {

    @NotBlank
    private String type;

    @NotNull
    private LocalDateTime startedAt;

    @Min(0)
    @Max(1440)
    private int durationInMinutes;

    @DecimalMin("0.0")
    @DecimalMax("1000.0")
    private double distanceKm;

    @ValidSteps
    private int steps;

    @ValidCaloriesBurned
    private int caloriesBurned;
}

