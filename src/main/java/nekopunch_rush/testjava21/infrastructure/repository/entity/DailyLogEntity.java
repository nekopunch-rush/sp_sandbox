package nekopunch_rush.testjava21.infrastructure.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyLogEntity {
    private Long userId;
    private LocalDate logDate;
    private double sleepHours;
    private int moodLevel;

    private List<BodyMetricsEntity> bodyMetricsList;
    private List<ActivityEntity> activityList;
    private List<MealEntity> mealList;
    private List<MentalNoteEntity> mentalNoteList;
}
