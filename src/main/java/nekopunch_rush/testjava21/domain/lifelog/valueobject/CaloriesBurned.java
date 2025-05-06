package nekopunch_rush.testjava21.domain.lifelog.valueobject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CaloriesBurned {
    private final int value;

    public static CaloriesBurned of(int value) {
        if (value < 0 || value > 10000) {
            throw new IllegalArgumentException("消費カロリーは0以上10000以下である必要があります。");
        }
        return new CaloriesBurned(value);
    }
}

