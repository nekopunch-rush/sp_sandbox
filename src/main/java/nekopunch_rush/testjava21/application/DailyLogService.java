package nekopunch_rush.testjava21.application;

import nekopunch_rush.testjava21.domain.lifelog.DailyLog;
import org.apache.ibatis.javassist.NotFoundException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public interface DailyLogService {

    void register(DailyLog log);

    void update(LocalDate logDate, DailyLog log) throws NotFoundException;

    void delete(Long userId, LocalDate logDate) throws NotFoundException;

    DailyLog findByUserIdAndDate(Long userId, LocalDate logDate) throws NotFoundException;

    List<DailyLog> findMonthlyByUserId(Long userId, YearMonth yearMonth);
}

