package nekopunch_rush.testjava21.presentation.resoource.lifelog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class DailyLogSummaryResponse {
    private LocalDate logDate;
    private double sleepHours;
    private int moodLevel;
}

