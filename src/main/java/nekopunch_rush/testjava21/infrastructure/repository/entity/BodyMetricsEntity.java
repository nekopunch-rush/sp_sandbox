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
public class BodyMetricsEntity {
    private Long userId;
    private LocalDate logDate;
    private double weight;
    private double bodyFatPercentage;
    private double muscleMass;
    private LocalDateTime measuredAt;
}
