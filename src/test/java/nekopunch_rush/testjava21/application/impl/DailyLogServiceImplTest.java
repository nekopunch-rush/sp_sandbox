package nekopunch_rush.testjava21.application.impl;

import nekopunch_rush.testjava21.domain.lifelog.DailyLog;
import nekopunch_rush.testjava21.infrastructure.repository.DailyLogRepository;
import org.apache.ibatis.javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DailyLogServiceImplTest {

    @Mock
    private DailyLogRepository repository;

    @InjectMocks
    private DailyLogServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("register: 既存データがない場合は正常に登録できる")
    void register_success() {
        DailyLog log = DailyLog.of(
                1L, 
                LocalDate.of(2024, 6, 1), 
                7.5, 
                4, 
                null,
                null,
                null,
                null);
        when(repository.existsByUserIdAndLogDate(1L, LocalDate.of(2024, 6, 1))).thenReturn(false);

        service.register(log);

        verify(repository).save(log);
    }

    @Test
    @DisplayName("register: 既に同日のログが存在する場合は例外")
    void register_alreadyExists() {
        DailyLog log = DailyLog.of(
                1L, 
                LocalDate.of(2024, 6, 1), 
                7.0, 
                3, 
                null,
                null,
                null,
                null);
        when(repository.existsByUserIdAndLogDate(1L, LocalDate.of(2024, 6, 1))).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.register(log));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("update: 対象データが存在する場合は正常に更新できる")
    void update_success() throws Exception {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2024, 6, 1);
        DailyLog updated = DailyLog.of(
                userId, 
                date, 
                6.0, 
                2, 
                null,
                null,
                null,
                null);
        when(repository.findByUserIdAndLogDate(userId, date)).thenReturn(Optional.of(
                DailyLog.of(
                        userId, 
                        date, 
                        7.0, 
                        3, 
                        null,
                        null,
                        null,
                        null)));

        service.update(userId, date, updated);

        verify(repository).update(userId, date, updated);
    }

    @Test
    @DisplayName("update: 対象データが存在しない場合は例外")
    void update_notFound() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2024, 6, 1);
        DailyLog updated = DailyLog.of(
                userId, 
                date, 
                5.0, 
                1, 
                null,
                null,
                null,
                null);
        when(repository.findByUserIdAndLogDate(userId, date)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.update(userId, date, updated));
        verify(repository, never()).update(any(), any(), any());
    }

    @Test
    @DisplayName("delete: 対象データが存在する場合は正常に削除できる")
    void delete_success() throws Exception {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2024, 6, 1);
        when(repository.existsByUserIdAndLogDate(userId, date)).thenReturn(true);

        service.delete(userId, date);

        verify(repository).deleteByUserIdAndLogDate(userId, date);
    }

    @Test
    @DisplayName("delete: 対象データが存在しない場合は例外")
    void delete_notFound() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2024, 6, 1);
        when(repository.existsByUserIdAndLogDate(userId, date)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.delete(userId, date));
        verify(repository, never()).deleteByUserIdAndLogDate(any(), any());
    }

    @Test
    @DisplayName("findByUserIdAndDate: 対象データが存在する場合は正常に取得できる")
    void findByUserIdAndDate_success() throws Exception {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2024, 6, 1);
        DailyLog log = DailyLog.of(
                userId, 
                date, 
                8.0, 
                5, 
                null,
                null,
                null,
                null);
        when(repository.findByUserIdAndLogDate(userId, date)).thenReturn(Optional.of(log));

        DailyLog result = service.findByUserIdAndDate(userId, date);

        assertEquals(log, result);
    }

    @Test
    @DisplayName("findByUserIdAndDate: 対象データが存在しない場合は例外")
    void findByUserIdAndDate_notFound() {
        Long userId = 1L;
        LocalDate date = LocalDate.of(2024, 6, 1);
        when(repository.findByUserIdAndLogDate(userId, date)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findByUserIdAndDate(userId, date));
    }

    @Test
    @DisplayName("findMonthlyByUserId: 指定月のデータ一覧が取得できる")
    void findMonthlyByUserId_success() {
        Long userId = 1L;
        YearMonth ym = YearMonth.of(2024, 6);
        List<DailyLog> logs = Arrays.asList(
                DailyLog.of(
                        userId, 
                        LocalDate.of(2024, 6, 1), 
                        7.0, 
                        3, 
                        null,
                        null,
                        null,
                        null),
                DailyLog.of(
                        userId, 
                        LocalDate.of(2024, 6, 2), 
                        6.5, 
                        4, 
                        null,
                        null,
                        null,
                        null)
        );
        when(repository.findByUserIdAndMonth(userId, ym)).thenReturn(logs);

        List<DailyLog> result = service.findMonthlyByUserId(userId, ym);

        assertEquals(logs, result);
    }
}
