package nekopunch_rush.testjava21.domain.lifelog.valueobject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Weight {
    private final double value;

    public static Weight of(double value) {
//        if (value <= 0 || value > 500) {
//            throw new IllegalArgumentException("体重は0より大きく500以下である必要があります。");
//        }
        return new Weight(value);
    }
}

