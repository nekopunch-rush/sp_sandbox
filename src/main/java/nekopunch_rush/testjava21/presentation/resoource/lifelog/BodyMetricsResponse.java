package nekopunch_rush.testjava21.presentation.resoource.lifelog;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BodyMetricsResponse {
    private double weight;
    private double bodyFatPercentage;
    private double muscleMass;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime measuredAt;
}

