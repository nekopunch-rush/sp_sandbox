package nekopunch_rush.testjava21.domain.lifelog.valueobject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BodyFatPercentage {
    private final double value;

    public static BodyFatPercentage of(double value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("体脂肪率は0%以上100%以下である必要があります。");
        }
        return new BodyFatPercentage(value);
    }
}

