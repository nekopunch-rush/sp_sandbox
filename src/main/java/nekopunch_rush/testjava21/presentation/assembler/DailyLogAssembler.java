package nekopunch_rush.testjava21.presentation.assembler;

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
import nekopunch_rush.testjava21.presentation.resoource.lifelog.ActivityRequest;
import nekopunch_rush.testjava21.presentation.resoource.lifelog.ActivityResponse;
import nekopunch_rush.testjava21.presentation.resoource.lifelog.BodyMetricsRequest;
import nekopunch_rush.testjava21.presentation.resoource.lifelog.BodyMetricsResponse;
import nekopunch_rush.testjava21.presentation.resoource.lifelog.DailyLogRequest;
import nekopunch_rush.testjava21.presentation.resoource.lifelog.DailyLogResponse;
import nekopunch_rush.testjava21.presentation.resoource.lifelog.MealRequest;
import nekopunch_rush.testjava21.presentation.resoource.lifelog.MealResponse;
import nekopunch_rush.testjava21.presentation.resoource.lifelog.MentalNoteRequest;
import nekopunch_rush.testjava21.presentation.resoource.lifelog.MentalNoteResponse;

import java.util.List;
import java.util.stream.Collectors;

public class DailyLogAssembler {

    public static DailyLog toDomain(DailyLogRequest request) {
        return DailyLog.of(
                request.getUserId(),
                request.getLogDate(),
                request.getSleepHours(),
                request.getMoodLevel(),
                toBodyMetricsList(request.getBodyMetricsList()),
                toActivityList(request.getActivityList()),
                toMealList(request.getMealList()),
                toMentalNoteList(request.getMentalNoteList())
        );
    }

    private static List<BodyMetrics> toBodyMetricsList(List<BodyMetricsRequest> list) {
        if (list == null) return List.of();
        return list.stream()
                .map(b -> BodyMetrics.of(
                        Weight.of(b.getWeight()),
                        BodyFatPercentage.of(b.getBodyFatPercentage()),
                        MuscleMass.of(b.getMuscleMass()),
                        MeasuredAt.of(b.getMeasuredAt())
                ))
                .collect(Collectors.toList());
    }

    private static List<Activity> toActivityList(List<ActivityRequest> list) {
        if (list == null) return List.of();
        return list.stream()
                .map(a -> Activity.of(
                        a.getType(),
                        MeasuredAt.of(a.getStartedAt()),
                        a.getDurationInMinutes(),
                        a.getDistanceKm(),
                        Steps.of(a.getSteps()),
                        CaloriesBurned.of(a.getCaloriesBurned())
                ))
                .collect(Collectors.toList());
    }

    private static List<Meal> toMealList(List<MealRequest> list) {
        if (list == null) return List.of();
        return list.stream()
                .map(m -> Meal.of(
                        m.getTime(),
                        m.getContent(),
                        m.getCalories(),
                        m.getPhotoUrl()
                ))
                .collect(Collectors.toList());
    }

    private static List<MentalNote> toMentalNoteList(List<MentalNoteRequest> list) {
        if (list == null) return List.of();
        return list.stream()
                .map(m -> MentalNote.of(
                        m.getRecordedAt(),
                        m.getStressLevel(),
                        m.getMotivationLevel(),
                        m.getNote()
                ))
                .collect(Collectors.toList());
    }

    public static DailyLogResponse toResponse(DailyLog log) {
        return DailyLogResponse.builder()
                .userId(log.getUserId())
                .logDate(log.getLogDate())
                .sleepHours(log.getSleepHours())
                .moodLevel(log.getMoodLevel())
                .bodyMetricsList(toBodyMetricsResponseList(log.getBodyMetricsList()))
                .activityList(toActivityResponseList(log.getActivityList()))
                .mealList(toMealResponseList(log.getMealList()))
                .mentalNoteList(toMentalNoteResponseList(log.getMentalNoteList()))
                .build();
    }

    private static List<BodyMetricsResponse> toBodyMetricsResponseList(List<BodyMetrics> list) {
        return list.stream()
                .map(b -> BodyMetricsResponse.builder()
                        .weight(b.getWeight().getValue())
                        .bodyFatPercentage(b.getBodyFatPercentage().getValue())
                        .muscleMass(b.getMuscleMass().getValue())
                        .measuredAt(b.getMeasuredAt().getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private static List<ActivityResponse> toActivityResponseList(List<Activity> list) {
        return list.stream()
                .map(a -> ActivityResponse.builder()
                        .type(a.getType())
                        .startedAt(a.getStartedAt().getValue())
                        .durationInMinutes(a.getDurationInMinutes())
                        .distanceKm(a.getDistanceKm())
                        .steps(a.getSteps().getValue())
                        .caloriesBurned(a.getCaloriesBurned().getValue())
                        .build())
                .collect(Collectors.toList());
    }

    private static List<MealResponse> toMealResponseList(List<Meal> list) {
        return list.stream()
                .map(m -> MealResponse.builder()
                        .time(m.getTime())
                        .content(m.getContent())
                        .calories(m.getCalories())
                        .photoUrl(m.getPhotoUrl())
                        .build())
                .collect(Collectors.toList());
    }

    private static List<MentalNoteResponse> toMentalNoteResponseList(List<MentalNote> list) {
        return list.stream()
                .map(m -> MentalNoteResponse.builder()
                        .recordedAt(m.getRecordedAt())
                        .stressLevel(m.getStressLevel())
                        .motivationLevel(m.getMotivationLevel())
                        .note(m.getNote())
                        .build())
                .collect(Collectors.toList());
    }
}

