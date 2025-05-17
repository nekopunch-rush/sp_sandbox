package nekopunch_rush.testjava21.infrastructure.repository.impl;

import nekopunch_rush.testjava21.domain.lifelog.DailyLog;
import nekopunch_rush.testjava21.infrastructure.repository.entity.DailyLogEntity;
import nekopunch_rush.testjava21.infrastructure.repository.mapper.DailyLogMapper;
import nekopunch_rush.testjava21.infrastructure.repository.mapper.DailyLogMapperSupport;
import nekopunch_rush.testjava21.infrastructure.repository.mapper.RelatedEntitiesMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DailyLogRepositoryImplのテストクラス
 */
class DailyLogRepositoryImplTest {

    private DailyLogMapper dailyLogMapper;
    private RelatedEntitiesMapper relatedEntitiesMapper;
    private DailyLogRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        dailyLogMapper = mock(DailyLogMapper.class);
        relatedEntitiesMapper = mock(RelatedEntitiesMapper.class);
        repository = new DailyLogRepositoryImpl(dailyLogMapper, relatedEntitiesMapper);
    }

    @Test
    void testSave() {
        DailyLog log = mock(DailyLog.class);
        DailyLogEntity entity = mock(DailyLogEntity.class);
        // ...必要に応じてDailyLogMapperSupportのstaticメソッドをMock化...

        try (var mocked = mockStatic(DailyLogMapperSupport.class)) {
            mocked.when(() -> DailyLogMapperSupport.toEntity(log)).thenReturn(entity);
            mocked.when(() -> DailyLogMapperSupport.toBodyMetricsEntities(log)).thenReturn(Collections.emptyList());
            mocked.when(() -> DailyLogMapperSupport.toActivityEntities(log)).thenReturn(Collections.emptyList());
            mocked.when(() -> DailyLogMapperSupport.toMealEntities(log)).thenReturn(Collections.emptyList());
            mocked.when(() -> DailyLogMapperSupport.toMentalNoteEntities(log)).thenReturn(Collections.emptyList());

            repository.save(log);

            verify(dailyLogMapper).insert(entity);
            verify(dailyLogMapper).insertBodyMetrics(Collections.emptyList());
            verify(dailyLogMapper).insertActivity(Collections.emptyList());
            verify(dailyLogMapper).insertMeal(Collections.emptyList());
            verify(dailyLogMapper).insertMentalNote(Collections.emptyList());
        }
    }

    @Test
    void testUpdate() {
        Long userId = 1L;
        LocalDate logDate = LocalDate.now();
        DailyLog log = mock(DailyLog.class);
        DailyLogEntity entity = mock(DailyLogEntity.class);

        when(log.getUserId()).thenReturn(userId);
        when(log.getLogDate()).thenReturn(logDate);

        try (var mocked = mockStatic(DailyLogMapperSupport.class)) {
            mocked.when(() -> DailyLogMapperSupport.toEntity(log)).thenReturn(entity);
            mocked.when(() -> DailyLogMapperSupport.toBodyMetricsEntities(log)).thenReturn(Collections.emptyList());
            mocked.when(() -> DailyLogMapperSupport.toActivityEntities(log)).thenReturn(Collections.emptyList());
            mocked.when(() -> DailyLogMapperSupport.toMealEntities(log)).thenReturn(Collections.emptyList());
            mocked.when(() -> DailyLogMapperSupport.toMentalNoteEntities(log)).thenReturn(Collections.emptyList());

            repository.update(logDate, log);

            verify(dailyLogMapper).update(logDate, entity);
            verify(dailyLogMapper).deleteBodyMetrics(userId, logDate);
            verify(dailyLogMapper).deleteActivity(userId, logDate);
            verify(dailyLogMapper).deleteMeal(userId, logDate);
            verify(dailyLogMapper).deleteMentalNote(userId, logDate);
            verify(dailyLogMapper).insertBodyMetrics(Collections.emptyList());
            verify(dailyLogMapper).insertActivity(Collections.emptyList());
            verify(dailyLogMapper).insertMeal(Collections.emptyList());
            verify(dailyLogMapper).insertMentalNote(Collections.emptyList());
        }
    }

    @Test
    void testDeleteByUserIdAndLogDate() {
        Long userId = 1L;
        LocalDate logDate = LocalDate.now();

        repository.deleteByUserIdAndLogDate(userId, logDate);

        verify(dailyLogMapper).delete(userId, logDate);
        verify(dailyLogMapper).deleteBodyMetrics(userId, logDate);
        verify(dailyLogMapper).deleteActivity(userId, logDate);
        verify(dailyLogMapper).deleteMeal(userId, logDate);
        verify(dailyLogMapper).deleteMentalNote(userId, logDate);
    }

    @Test
    void testFindByUserIdAndLogDate_found() {
        Long userId = 1L;
        LocalDate logDate = LocalDate.now();
        DailyLogEntity entity = mock(DailyLogEntity.class);

        when(dailyLogMapper.findByUserIdAndLogDate(userId, logDate)).thenReturn(entity);
        when(relatedEntitiesMapper.selectBodyMetrics(userId, logDate)).thenReturn(Collections.emptyList());
        when(relatedEntitiesMapper.selectActivity(userId, logDate)).thenReturn(Collections.emptyList());
        when(relatedEntitiesMapper.selectMeal(userId, logDate)).thenReturn(Collections.emptyList());
        when(relatedEntitiesMapper.selectMentalNote(userId, logDate)).thenReturn(Collections.emptyList());

        DailyLog domain = mock(DailyLog.class);

        try (var mocked = mockStatic(DailyLogMapperSupport.class)) {
            mocked.when(() -> DailyLogMapperSupport.toDomain(entity)).thenReturn(domain);

            Optional<DailyLog> result = repository.findByUserIdAndLogDate(userId, logDate);

            assertTrue(result.isPresent());
            assertEquals(domain, result.get());
        }
    }

    @Test
    void testFindByUserIdAndLogDate_notFound() {
        Long userId = 1L;
        LocalDate logDate = LocalDate.now();

        when(dailyLogMapper.findByUserIdAndLogDate(userId, logDate)).thenReturn(null);

        Optional<DailyLog> result = repository.findByUserIdAndLogDate(userId, logDate);

        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByUserIdAndMonth() {
        Long userId = 1L;
        YearMonth ym = YearMonth.now();
        DailyLogEntity entity = mock(DailyLogEntity.class);
        List<DailyLogEntity> entities = List.of(entity);
        DailyLog summary = mock(DailyLog.class);

        when(dailyLogMapper.findByUserIdAndMonth(userId, ym.getYear(), ym.getMonthValue())).thenReturn(entities);

        try (var mocked = mockStatic(DailyLogMapperSupport.class)) {
            mocked.when(() -> DailyLogMapperSupport.toDomainSummary(entity)).thenReturn(summary);

            List<DailyLog> result = repository.findByUserIdAndMonth(userId, ym);

            assertEquals(1, result.size());
            assertEquals(summary, result.getFirst());
        }
    }

    @Test
    void testExistsByUserIdAndLogDate_true() {
        Long userId = 1L;
        LocalDate logDate = LocalDate.now();

        when(dailyLogMapper.countByUserIdAndLogDate(userId, logDate)).thenReturn(1);

        assertTrue(repository.existsByUserIdAndLogDate(userId, logDate));
    }

    @Test
    void testExistsByUserIdAndLogDate_false() {
        Long userId = 1L;
        LocalDate logDate = LocalDate.now();

        when(dailyLogMapper.countByUserIdAndLogDate(userId, logDate)).thenReturn(0);

        assertFalse(repository.existsByUserIdAndLogDate(userId, logDate));
    }
}
