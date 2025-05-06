package nekopunch_rush.testjava21.presentation.resoource.lifelog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyLogRequest {

    @NotNull
    private Long userId;

    @NotNull
    private LocalDate logDate;

    @Min(0)
    @Max(24)
    private double sleepHours;

    @Min(1)
    @Max(10)
    private int moodLevel;

    @Valid
    private List<BodyMetricsRequest> bodyMetricsList;

    @Valid
    private List<ActivityRequest> activityList;

    @Valid
    private List<MealRequest> mealList;

    @Valid
    private List<MentalNoteRequest> mentalNoteList;
}

