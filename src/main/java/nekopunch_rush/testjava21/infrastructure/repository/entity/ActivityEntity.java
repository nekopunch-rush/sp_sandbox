package nekopunch_rush.testjava21.infrastructure.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityEntity {
    private Long userId;
    private LocalDate logDate;
    private String type;
    private LocalDateTime startedAt;
    private int durationInMinutes;
    private double distanceKm;
    private int steps;
    private int caloriesBurned;
}
