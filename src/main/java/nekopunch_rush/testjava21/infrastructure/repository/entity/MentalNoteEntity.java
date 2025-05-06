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
public class MentalNoteEntity {
    private Long userId;
    private LocalDate logDate;
    private LocalDateTime recordedAt;
    private int stressLevel;
    private int motivationLevel;
    private String note;
}
