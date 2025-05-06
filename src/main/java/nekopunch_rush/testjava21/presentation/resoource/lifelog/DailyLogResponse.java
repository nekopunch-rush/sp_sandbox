package nekopunch_rush.testjava21.presentation.resoource.lifelog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class DailyLogResponse {

    private Long userId;
    private LocalDate logDate;
    private double sleepHours;
    private int moodLevel;

    private List<BodyMetricsResponse> bodyMetricsList;
    private List<ActivityResponse> activityList;
    private List<MealResponse> mealList;
    private List<MentalNoteResponse> mentalNoteList;
}

