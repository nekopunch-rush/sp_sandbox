package nekopunch_rush.testjava21.application.impl;

import lombok.RequiredArgsConstructor;
import nekopunch_rush.testjava21.application.DailyLogService;
import nekopunch_rush.testjava21.domain.lifelog.DailyLog;
import nekopunch_rush.testjava21.infrastructure.repository.DailyLogRepository;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyLogServiceImpl implements DailyLogService {

    private final DailyLogRepository repository;

    @Override
    public void register(DailyLog log) {
        if (repository.existsByUserIdAndLogDate(log.getUserId(), log.getLogDate())) {
            throw new IllegalStateException("既に同日のログが存在します。");
        }
        repository.save(log);
    }

    @Override
    public void update(Long userId, LocalDate logDate, DailyLog updatedLog) throws NotFoundException {
        DailyLog existing = repository.findByUserIdAndLogDate(userId, logDate)
                .orElseThrow(() -> new NotFoundException("指定されたログが存在しません。"));

        // 更新処理（置き換え or 差分更新）
        repository.update(userId, logDate, updatedLog);
    }

    @Override
    public void delete(Long userId, LocalDate logDate) throws NotFoundException {
        if (!repository.existsByUserIdAndLogDate(userId, logDate)) {
            throw new NotFoundException("指定されたログが存在しません。");
        }
        repository.deleteByUserIdAndLogDate(userId, logDate);
    }

    @Override
    public DailyLog findByUserIdAndDate(Long userId, LocalDate logDate) throws NotFoundException {
        return repository.findByUserIdAndLogDate(userId, logDate)
                .orElseThrow(() -> new NotFoundException("指定されたログが見つかりません。"));
    }

    @Override
    public List<DailyLog> findMonthlyByUserId(Long userId, YearMonth yearMonth) {
        return repository.findByUserIdAndMonth(userId, yearMonth);
    }
}


