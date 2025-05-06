package nekopunch_rush.testjava21.domain.lifelog;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MentalNote {

    private final LocalDateTime recordedAt;
    private final int stressLevel;     // 0〜10
    private final int motivationLevel; // 0〜10
    private final String note;         // 任意メモ

    public static MentalNote of(LocalDateTime recordedAt, int stressLevel, int motivationLevel, String note) {
        if (recordedAt == null) {
            throw new IllegalArgumentException("記録時刻は必須です");
        }
        if (stressLevel < 0 || stressLevel > 10) {
            throw new IllegalArgumentException("ストレスレベルは0〜10の範囲で指定してください");
        }
        if (motivationLevel < 0 || motivationLevel > 10) {
            throw new IllegalArgumentException("モチベーションは0〜10の範囲で指定してください");
        }
        return new MentalNote(recordedAt, stressLevel, motivationLevel, note);
    }
}

