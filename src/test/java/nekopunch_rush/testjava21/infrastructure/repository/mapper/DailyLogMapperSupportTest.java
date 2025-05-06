package nekopunch_rush.testjava21.infrastructure.repository.mapper;

import nekopunch_rush.testjava21.domain.lifelog.*;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.*;
import nekopunch_rush.testjava21.infrastructure.repository.entity.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DailyLogMapperSupportTest {

    @Test
    void testToEntity() {
        DailyLog log = DailyLog.of(1L, LocalDate.of(2024, 6, 1), 7.5, 4, List.of(), List.of(), List.of(), List.of());
        DailyLogEntity entity = DailyLogMapperSupport.toEntity(log);
        assertEquals(1L, entity.getUserId());
        assertEquals(LocalDate.of(2024, 6, 1), entity.getLogDate());
        assertEquals(7.5, entity.getSleepHours());
        assertEquals(4, entity.getMoodLevel());
    }

    @Test
    void testToBodyMetricsEntities() {
        BodyMetrics bm = BodyMetrics.of(
                Weight.of(60.0),
                BodyFatPercentage.of(20.0),
                MuscleMass.of(30.0),
                MeasuredAt.of(LocalDateTime.of(2024, 6, 1, 8, 0))
        );
        DailyLog log = DailyLog.of(1L, LocalDate.now(), 0, 1, List.of(bm), List.of(), List.of(), List.of());
        List<BodyMetricsEntity> entities = DailyLogMapperSupport.toBodyMetricsEntities(log);
        assertEquals(1, entities.size());
        BodyMetricsEntity entity = entities.getFirst();
        assertEquals(60.0, entity.getWeight());
        assertEquals(20.0, entity.getBodyFatPercentage());
        assertEquals(30.0, entity.getMuscleMass());
        assertEquals("2024-06-01T08:00", entity.getMeasuredAt().toString());
    }

    @Test
    void testToActivityEntities() {
        Activity act = Activity.of(
                "run",
                MeasuredAt.of(LocalDateTime.of(2024, 6, 1, 9, 0)),
                30,
                5.0,
                Steps.of(6000),
                CaloriesBurned.of(300)
        );
        DailyLog log = DailyLog.of(1L, LocalDate.now(), 0, 1, List.of(), List.of(act), List.of(), List.of());
        List<ActivityEntity> entities = DailyLogMapperSupport.toActivityEntities(log);
        assertEquals(1, entities.size());
        ActivityEntity entity = entities.getFirst();
        assertEquals("run", entity.getType());
        assertEquals("2024-06-01T09:00", entity.getStartedAt().toString());
        assertEquals(30, entity.getDurationInMinutes());
        assertEquals(5.0, entity.getDistanceKm());
        assertEquals(6000, entity.getSteps());
        assertEquals(300, entity.getCaloriesBurned());
    }

    @Test
    void testToMealEntities() {
        Meal meal = Meal.of(LocalTime.of(12, 0, 0), "Lunch", 700, "photo.jpg");
        DailyLog log = DailyLog.of(1L, LocalDate.now(), 0, 1, List.of(), List.of(), List.of(meal), List.of());
        List<MealEntity> entities = DailyLogMapperSupport.toMealEntities(log);
        assertEquals(1, entities.size());
        MealEntity entity = entities.getFirst();
        assertEquals("12:00", entity.getTime().toString());
        assertEquals("Lunch", entity.getContent());
        assertEquals(700, entity.getCalories());
        assertEquals("photo.jpg", entity.getPhotoUrl());
    }

    @Test
    void testToMentalNoteEntities() {
        MentalNote note = MentalNote.of(LocalDateTime.of(2024, 6, 1, 10, 0), 2, 5, "note");
        DailyLog log = DailyLog.of(1L, LocalDate.now(), 0, 1, List.of(), List.of(), List.of(), List.of(note));
        List<MentalNoteEntity> entities = DailyLogMapperSupport.toMentalNoteEntities(log);
        assertEquals(1, entities.size());
        MentalNoteEntity entity = entities.getFirst();
        assertEquals("2024-06-01T10:00", entity.getRecordedAt().toString());
        assertEquals(2, entity.getStressLevel());
        assertEquals(5, entity.getMotivationLevel());
        assertEquals("note", entity.getNote());
    }

    @Test
    void testToDomain() {
        BodyMetricsEntity bm = BodyMetricsEntity.builder()
                .weight(60.0).bodyFatPercentage(20.0).muscleMass(30.0).measuredAt(LocalDateTime.of(2024, 6, 1, 8, 0))
                .build();
        ActivityEntity act = ActivityEntity.builder()
                .type("run").startedAt(LocalDateTime.of(2024, 6, 1, 9, 0)).durationInMinutes(30).distanceKm(5.0).steps(6000).caloriesBurned(300)
                .build();
        MealEntity meal = MealEntity.builder()
                .time(LocalTime.of(12, 0, 0)).content("Lunch").calories(700).photoUrl("photo.jpg")
                .build();
        MentalNoteEntity note = MentalNoteEntity.builder()
                .recordedAt(LocalDateTime.of(2024, 6, 1, 10, 0)).stressLevel(2).motivationLevel(5).note("note")
                .build();
        DailyLogEntity entity = DailyLogEntity.builder()
                .userId(1L).logDate(LocalDate.of(2024, 6, 1)).sleepHours(7.5).moodLevel(4)
                .bodyMetricsList(List.of(bm))
                .activityList(List.of(act))
                .mealList(List.of(meal))
                .mentalNoteList(List.of(note))
                .build();
        DailyLog log = DailyLogMapperSupport.toDomain(entity);
        assertEquals(1L, log.getUserId());
        assertEquals(LocalDate.of(2024, 6, 1), log.getLogDate());
        assertEquals(7.5, log.getSleepHours());
        assertEquals(4, log.getMoodLevel());
        assertEquals(1, log.getBodyMetricsList().size());
        assertEquals(1, log.getActivityList().size());
        assertEquals(1, log.getMealList().size());
        assertEquals(1, log.getMentalNoteList().size());
    }

    @Test
    void testToDomainSummary() {
        DailyLogEntity entity = DailyLogEntity.builder()
                .userId(1L).logDate(LocalDate.of(2024, 6, 1)).sleepHours(7.5).moodLevel(4)
                .build();
        DailyLog log = DailyLogMapperSupport.toDomainSummary(entity);
        assertEquals(1L, log.getUserId());
        assertEquals(LocalDate.of(2024, 6, 1), log.getLogDate());
        assertEquals(7.5, log.getSleepHours());
        assertEquals(4, log.getMoodLevel());
        assertTrue(log.getBodyMetricsList().isEmpty());
        assertTrue(log.getActivityList().isEmpty());
        assertTrue(log.getMealList().isEmpty());
        assertTrue(log.getMentalNoteList().isEmpty());
    }
}
