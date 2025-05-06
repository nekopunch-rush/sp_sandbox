package nekopunch_rush.testjava21.domain.lifelog;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DailyLog {

    private final Long userId;              // 所有ユーザー
    private final LocalDate logDate;        // ログ対象日
    private final double sleepHours;        // 睡眠時間（時間単位）
    private final int moodLevel;            // 気分（1〜10）

    private final List<BodyMetrics> bodyMetricsList;
    private final List<Activity> activityList;
    private final List<Meal> mealList;
    private final List<MentalNote> mentalNoteList;

    public static DailyLog of(
            Long userId,
            LocalDate logDate,
            double sleepHours,
            int moodLevel,
            List<BodyMetrics> bodyMetricsList,
            List<Activity> activityList,
            List<Meal> mealList,
            List<MentalNote> mentalNoteList
    ) {
        if (userId == null) {
            throw new IllegalArgumentException("ユーザーIDは必須です");
        }
        if (logDate == null) {
            throw new IllegalArgumentException("ログ日付は必須です");
        }
        if (sleepHours < 0 || sleepHours > 24) {
            throw new IllegalArgumentException("睡眠時間は0〜24時間で指定してください");
        }
        if (moodLevel < 1 || moodLevel > 10) {
            throw new IllegalArgumentException("気分は1〜10の範囲で指定してください");
        }

        return new DailyLog(
                userId,
                logDate,
                sleepHours,
                moodLevel,
                bodyMetricsList != null ? bodyMetricsList : Collections.emptyList(),
                activityList != null ? activityList : Collections.emptyList(),
                mealList != null ? mealList : Collections.emptyList(),
                mentalNoteList != null ? mentalNoteList : Collections.emptyList()
        );
    }
}

