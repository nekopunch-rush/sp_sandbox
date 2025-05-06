package nekopunch_rush.testjava21.domain.lifelog;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.CaloriesBurned;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.MeasuredAt;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.Steps;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Activity {

    private final String type; // ウォーキング・ジョギング・筋トレなど
    private final MeasuredAt startedAt;
    private final int durationInMinutes; // 活動時間
    private final double distanceKm;     // km単位（0.0なら不要）
    private final Steps steps;
    private final CaloriesBurned caloriesBurned;

    public static Activity of(
            String type,
            MeasuredAt startedAt,
            int durationInMinutes,
            double distanceKm,
            Steps steps,
            CaloriesBurned caloriesBurned
    ) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("アクティビティ種別は必須です");
        }
        if (durationInMinutes < 0 || durationInMinutes > 1440) {
            throw new IllegalArgumentException("活動時間は0〜1440分の範囲で指定してください");
        }
        if (distanceKm < 0 || distanceKm > 1000) {
            throw new IllegalArgumentException("距離は0km〜1000kmの範囲で指定してください");
        }
        return new Activity(type, startedAt, durationInMinutes, distanceKm, steps, caloriesBurned);
    }
}
