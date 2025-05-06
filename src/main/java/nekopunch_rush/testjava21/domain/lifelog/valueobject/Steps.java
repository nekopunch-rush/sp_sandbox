package nekopunch_rush.testjava21.domain.lifelog.valueobject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Steps {
    private final int value;

    public static Steps of(int value) {
        if (value < 0 || value > 100000) {
            throw new IllegalArgumentException("歩数は0以上100000以下である必要があります。");
        }
        return new Steps(value);
    }
}

