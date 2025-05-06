package nekopunch_rush.testjava21.presentation.resoource.lifelog;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.validation.ValidBodyFat;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.validation.ValidMuscleMass;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.validation.ValidWeight;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BodyMetricsRequest {

    @ValidWeight
    private double weight;

    @ValidBodyFat
    private double bodyFatPercentage;

    @ValidMuscleMass
    private double muscleMass;

    @NotNull
    private LocalDateTime measuredAt;
}

