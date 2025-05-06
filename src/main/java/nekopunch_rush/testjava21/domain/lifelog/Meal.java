package nekopunch_rush.testjava21.domain.lifelog;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Meal {

    private final LocalTime time;       // 食事の時間
    private final String content;       // 内容（ラーメン、サラダなど）
    private final int calories;         // 推定カロリー
    private final String photoUrl;      // 任意、写真URL

    public static Meal of(LocalTime time, String content, int calories, String photoUrl) {
        if (time == null) {
            throw new IllegalArgumentException("食事時間は必須です");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("食事内容は必須です");
        }
        if (calories < 0 || calories > 10000) {
            throw new IllegalArgumentException("カロリーは0〜10000の範囲で指定してください");
        }
        return new Meal(time, content, calories, photoUrl);
    }
}
