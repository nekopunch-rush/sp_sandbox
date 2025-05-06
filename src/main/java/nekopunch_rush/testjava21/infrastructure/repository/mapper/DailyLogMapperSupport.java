package nekopunch_rush.testjava21.infrastructure.repository.mapper;


import nekopunch_rush.testjava21.domain.lifelog.Activity;
import nekopunch_rush.testjava21.domain.lifelog.BodyMetrics;
import nekopunch_rush.testjava21.domain.lifelog.DailyLog;
import nekopunch_rush.testjava21.domain.lifelog.Meal;
import nekopunch_rush.testjava21.domain.lifelog.MentalNote;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.BodyFatPercentage;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.CaloriesBurned;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.MeasuredAt;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.MuscleMass;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.Steps;
import nekopunch_rush.testjava21.domain.lifelog.valueobject.Weight;
import nekopunch_rush.testjava21.infrastructure.repository.entity.ActivityEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.BodyMetricsEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.DailyLogEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.MealEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.MentalNoteEntity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Optional;

public class DailyLogMapperSupport {

    public static DailyLogEntity toEntity(DailyLog log) {
        return DailyLogEntity.builder()
                .userId(log.getUserId())
                .logDate(log.getLogDate())
                .sleepHours(log.getSleepHours())
                .moodLevel(log.getMoodLevel())
                .build();
    }

    public static List<BodyMetricsEntity> toBodyMetricsEntities(DailyLog log) {
        return log.getBodyMetricsList().stream()
                .map(m -> BodyMetricsEntity.builder()
                        .userId(log.getUserId())
                        .logDate(log.getLogDate())
                        .weight(m.getWeight().getValue())
                        .bodyFatPercentage(m.getBodyFatPercentage().getValue())
                        .muscleMass(m.getMuscleMass().getValue())
                        .measuredAt(m.getMeasuredAt().getValue())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<ActivityEntity> toActivityEntities(DailyLog log) {
        return log.getActivityList().stream()
                .map(a -> ActivityEntity.builder()
                        .userId(log.getUserId())
                        .logDate(log.getLogDate())
                        .type(a.getType())
                        .startedAt(a.getStartedAt().getValue())
                        .durationInMinutes(a.getDurationInMinutes())
                        .distanceKm(a.getDistanceKm())
                        .steps(a.getSteps().getValue())
                        .caloriesBurned(a.getCaloriesBurned().getValue())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<MealEntity> toMealEntities(DailyLog log) {
        return log.getMealList().stream()
                .map(m -> MealEntity.builder()
                        .userId(log.getUserId())
                        .logDate(log.getLogDate())
                        .time(m.getTime())
                        .content(m.getContent())
                        .calories(m.getCalories())
                        .photoUrl(m.getPhotoUrl())
                        .build())
                .collect(Collectors.toList());
    }

    public static List<MentalNoteEntity> toMentalNoteEntities(DailyLog log) {
        return log.getMentalNoteList().stream()
                .map(m -> MentalNoteEntity.builder()
                        .userId(log.getUserId())
                        .logDate(log.getLogDate())
                        .recordedAt(m.getRecordedAt())
                        .stressLevel(m.getStressLevel())
                        .motivationLevel(m.getMotivationLevel())
                        .note(m.getNote())
                        .build())
                .collect(Collectors.toList());
    }

    public static DailyLog toDomain(DailyLogEntity entity) {
        List<BodyMetrics> bodyMetricsList = Optional.ofNullable(entity.getBodyMetricsList())
                .orElse(Collections.emptyList())
                .stream()
                .map(e -> BodyMetrics.of(
                        Weight.of(e.getWeight()),
                        BodyFatPercentage.of(e.getBodyFatPercentage()),
                        MuscleMass.of(e.getMuscleMass()),
                        MeasuredAt.of(e.getMeasuredAt())
                ))
                .toList();

        List<Activity> activityList = Optional.ofNullable(entity.getActivityList())
                .orElse(Collections.emptyList())
                .stream()
                .map(e -> Activity.of(
                        e.getType(),
                        MeasuredAt.of(e.getStartedAt()),
                        e.getDurationInMinutes(),
                        e.getDistanceKm(),
                        Steps.of(e.getSteps()),
                        CaloriesBurned.of(e.getCaloriesBurned())
                ))
                .toList();

        List<Meal> mealList = Optional.ofNullable(entity.getMealList())
                .orElse(Collections.emptyList())
                .stream()
                .map(e -> Meal.of(
                        e.getTime(),
                        e.getContent(),
                        e.getCalories(),
                        e.getPhotoUrl()
                ))
                .toList();

        List<MentalNote> mentalNoteList = Optional.ofNullable(entity.getMentalNoteList())
                .orElse(Collections.emptyList())
                .stream()
                .map(e -> MentalNote.of(
                        e.getRecordedAt(),
                        e.getStressLevel(),
                        e.getMotivationLevel(),
                        e.getNote()
                ))
                .toList();

        return DailyLog.of(
                entity.getUserId(),
                entity.getLogDate(),
                entity.getSleepHours(),
                entity.getMoodLevel(),
                bodyMetricsList,
                activityList,
                mealList,
                mentalNoteList
        );
    }

    public static DailyLog toDomainSummary(DailyLogEntity entity) {
        return DailyLog.of(
                entity.getUserId(),
                entity.getLogDate(),
                entity.getSleepHours(),
                entity.getMoodLevel(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }
}
