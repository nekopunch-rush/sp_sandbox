package nekopunch_rush.testjava21.domain.lifelog.valueobject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MuscleMass {

    private static final double MIN_MUSCLE_MASS = 0;
    private static final double MAX_MUSCLE_MASS = 100;

    private final double value;

    public static MuscleMass of(double value) {
        if (value < MIN_MUSCLE_MASS || value > MAX_MUSCLE_MASS) {
            throw new IllegalArgumentException("筋肉量は" + MIN_MUSCLE_MASS + "以上" + MAX_MUSCLE_MASS + "以下である必要があります。");
        }
        return new MuscleMass(value);
    }
}

