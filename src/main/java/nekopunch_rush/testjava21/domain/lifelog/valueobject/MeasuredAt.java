package nekopunch_rush.testjava21.domain.lifelog.valueobject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MeasuredAt {
    private final LocalDateTime value;

    public static MeasuredAt of(LocalDateTime value) {
        if (value == null) {
            throw new IllegalArgumentException("計測日時は必須です。");
        }
        return new MeasuredAt(value);
    }
}

