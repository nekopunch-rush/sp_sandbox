package nekopunch_rush.testjava21.infrastructure.repository.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealEntity {
    private Long userId;
    private LocalDate logDate;
    private LocalTime time;
    private String content;
    private int calories;
    private String photoUrl;
}

