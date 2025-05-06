package nekopunch_rush.testjava21.presentation.resoource.lifelog;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ActivityResponse {

    private String type;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;

    private int durationInMinutes;
    private double distanceKm;
    private int steps;
    private int caloriesBurned;
}

