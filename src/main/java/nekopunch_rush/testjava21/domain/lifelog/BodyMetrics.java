package nekopunch_rush.testjava21.domain.lifelog;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.BodyFatPercentage;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.MeasuredAt;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.MuscleMass;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.Weight;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BodyMetrics {

    private final Weight weight;
    private final BodyFatPercentage bodyFatPercentage;
    private final MuscleMass muscleMass;
    private final MeasuredAt measuredAt;

    public static BodyMetrics of(
            Weight weight,
            BodyFatPercentage bodyFatPercentage,
            MuscleMass muscleMass,
            MeasuredAt measuredAt
    ) {
        // ここで集約内ルールを追加することも可能（例：体脂肪と筋肉量の合計が100%超えてはならない 等）
        return new BodyMetrics(weight, bodyFatPercentage, muscleMass, measuredAt);
    }
}
