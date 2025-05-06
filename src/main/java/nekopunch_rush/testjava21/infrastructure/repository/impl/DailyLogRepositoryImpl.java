package nekopunch_rush.testjava21.infrastructure.repository.impl;

import lombok.RequiredArgsConstructor;
import nekopunch_rush.testjava21.domain.lifelog.DailyLog;
import nekopunch_rush.testjava21.infrastructure.repository.DailyLogRepository;
import nekopunch_rush.testjava21.infrastructure.repository.entity.DailyLogEntity;
import nekopunch_rush.testjava21.infrastructure.repository.mapper.DailyLogMapper;
import nekopunch_rush.testjava21.infrastructure.repository.mapper.DailyLogMapperSupport;
import nekopunch_rush.testjava21.infrastructure.repository.mapper.RelatedEntitiesMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DailyLogRepositoryImpl implements DailyLogRepository {

    private final DailyLogMapper dailyLogMapper;
    private final RelatedEntitiesMapper relatedEntitiesMapper;

    @Override
    public void save(DailyLog log) {
        DailyLogEntity entity = DailyLogMapperSupport.toEntity(log);
        dailyLogMapper.insert(entity);

        dailyLogMapper.insertBodyMetrics(DailyLogMapperSupport.toBodyMetricsEntities(log));
        dailyLogMapper.insertActivity(DailyLogMapperSupport.toActivityEntities(log));
        dailyLogMapper.insertMeal(DailyLogMapperSupport.toMealEntities(log));
        dailyLogMapper.insertMentalNote(DailyLogMapperSupport.toMentalNoteEntities(log));
    }

    @Override
    public void update(Long userId, LocalDate logDate, DailyLog log) {
        DailyLogEntity entity = DailyLogMapperSupport.toEntity(log);
        dailyLogMapper.update(userId, logDate, entity);

        // 削除して再登録（差分更新しない方針）
        dailyLogMapper.deleteBodyMetrics(userId, logDate);
        dailyLogMapper.deleteActivity(userId, logDate);
        dailyLogMapper.deleteMeal(userId, logDate);
        dailyLogMapper.deleteMentalNote(userId, logDate);

        dailyLogMapper.insertBodyMetrics(DailyLogMapperSupport.toBodyMetricsEntities(log));
        dailyLogMapper.insertActivity(DailyLogMapperSupport.toActivityEntities(log));
        dailyLogMapper.insertMeal(DailyLogMapperSupport.toMealEntities(log));
        dailyLogMapper.insertMentalNote(DailyLogMapperSupport.toMentalNoteEntities(log));
    }

    @Override
    public void deleteByUserIdAndLogDate(Long userId, LocalDate logDate) {
        dailyLogMapper.delete(userId, logDate);
        dailyLogMapper.deleteBodyMetrics(userId, logDate);
        dailyLogMapper.deleteActivity(userId, logDate);
        dailyLogMapper.deleteMeal(userId, logDate);
        dailyLogMapper.deleteMentalNote(userId, logDate);
    }

    @Override
    public Optional<DailyLog> findByUserIdAndLogDate(Long userId, LocalDate logDate) {
        DailyLogEntity logEntity = dailyLogMapper.findByUserIdAndLogDate(userId, logDate);
        if (logEntity == null) return Optional.empty();

        logEntity.setBodyMetricsList(relatedEntitiesMapper.selectBodyMetrics(userId, logDate));
        logEntity.setActivityList(relatedEntitiesMapper.selectActivity(userId, logDate));
        logEntity.setMealList(relatedEntitiesMapper.selectMeal(userId, logDate));
        logEntity.setMentalNoteList(relatedEntitiesMapper.selectMentalNote(userId, logDate));

        return Optional.of(DailyLogMapperSupport.toDomain(logEntity));
    }

    @Override
    public List<DailyLog> findByUserIdAndMonth(Long userId, YearMonth yearMonth) {
        List<DailyLogEntity> entities = dailyLogMapper.findByUserIdAndMonth(userId, yearMonth.getYear(), yearMonth.getMonthValue());
        return entities.stream()
                .map(DailyLogMapperSupport::toDomainSummary)
                .toList();
    }

    @Override
    public boolean existsByUserIdAndLogDate(Long userId, LocalDate logDate) {
        return dailyLogMapper.countByUserIdAndLogDate(userId, logDate) > 0;
    }
}
