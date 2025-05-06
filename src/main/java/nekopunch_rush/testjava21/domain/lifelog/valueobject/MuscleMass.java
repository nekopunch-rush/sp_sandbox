package nekopunch_rush.testjava21.domain.lifelog.valueobject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MuscleMass {
    private final double value;

    public static MuscleMass of(double value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("筋肉量は0以上100以下である必要があります。");
        }
        return new MuscleMass(value);
    }
}

