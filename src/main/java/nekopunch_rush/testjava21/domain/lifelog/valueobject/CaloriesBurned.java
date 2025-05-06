package nekopunch_rush.testjava21.domain.lifelog.valueobject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CaloriesBurned {

    private static final int MIN_CALORIES = 0;
    private static final int MAX_CALORIES = 10000;

    private final int value;

    public static CaloriesBurned of(int value) {
        if (value < MIN_CALORIES || value > MAX_CALORIES) {
            throw new IllegalArgumentException("消費カロリーは" + MIN_CALORIES + "以上" + MAX_CALORIES + "以下である必要があります。");
        }
        return new CaloriesBurned(value);
    }
}

