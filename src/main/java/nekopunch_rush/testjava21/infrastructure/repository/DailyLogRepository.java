package nekopunch_rush.testjava21.infrastructure.repository;

import nekopunch_rush.testjava21.domain.lifelog.DailyLog;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface DailyLogRepository {
    void save(DailyLog log);
    void update(Long userId, LocalDate logDate, DailyLog log);
    void deleteByUserIdAndLogDate(Long userId, LocalDate logDate);
    Optional<DailyLog> findByUserIdAndLogDate(Long userId, LocalDate logDate);
    List<DailyLog> findByUserIdAndMonth(Long userId, YearMonth yearMonth);
    boolean existsByUserIdAndLogDate(Long userId, LocalDate logDate);
}

