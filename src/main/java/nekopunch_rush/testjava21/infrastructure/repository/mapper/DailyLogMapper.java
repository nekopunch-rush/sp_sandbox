package nekopunch_rush.testjava21.infrastructure.repository.mapper;

import nekopunch_rush.testjava21.infrastructure.repository.entity.ActivityEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.BodyMetricsEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.DailyLogEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.MealEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.MentalNoteEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DailyLogMapper {

//    @Insert("INSERT INTO daily_log (user_id, log_date, sleep_hours, mood_level) " +
//            "VALUES (#{log.userId}, #{log.logDate}, #{log.sleepHours}, #{log.moodLevel})")
//    void inssertTest(@Param("log") DailyLogEntity log);
//
//    @Select("SELECT user_id, log_date, sleep_hours, mood_level FROM daily_log WHERE user_id = #{userId} AND log_date = #{logDate}")
//    @Results(id = "1L", value = {
//            @Result(property = "userId", column = "user_id"),
//            @Result(property = "logDate", column = "log_date"),
//            @Result(property = "sleepHours", column = "sleep_hours"),
//            @Result(property = "moodLevel", column = "mood_level")
//    })
//    DailyLogEntity selectTest(@Param("userId") Long userId, @Param("logDate") LocalDate logDate);

    // DailyLog
    void insert(@Param("log") DailyLogEntity log);

    void update(@Param("userId") Long userId,
                @Param("logDate") LocalDate logDate,
                @Param("log") DailyLogEntity log);

    void delete(@Param("userId") Long userId,
                @Param("logDate") LocalDate logDate);

    DailyLogEntity findByUserIdAndLogDate(@Param("userId") Long userId,
                                          @Param("logDate") LocalDate logDate);

    List<DailyLogEntity> findByUserIdAndMonth(@Param("userId") Long userId,
                                              @Param("year") int year,
                                              @Param("month") int month);

    int countByUserIdAndLogDate(@Param("userId") Long userId,
                                @Param("logDate") LocalDate logDate);

    // BodyMetrics
    void insertBodyMetrics(@Param("metrics") List<BodyMetricsEntity> metrics);
    void updateBodyMetrics(@Param("metrics") List<BodyMetricsEntity> metrics);
    void deleteBodyMetrics(@Param("userId") Long userId, @Param("logDate") LocalDate logDate);

    // Activity
    void insertActivity(@Param("activities") List<ActivityEntity> activities);
    void updateActivity(@Param("activities") List<ActivityEntity> activities);
    void deleteActivity(@Param("userId") Long userId, @Param("logDate") LocalDate logDate);

    // Meal
    void insertMeal(@Param("meals") List<MealEntity> meals);
    void updateMeal(@Param("meals") List<MealEntity> meals);
    void deleteMeal(@Param("userId") Long userId, @Param("logDate") LocalDate logDate);

    // MentalNote
    void insertMentalNote(@Param("notes") List<MentalNoteEntity> notes);
    void updateMentalNote(@Param("notes") List<MentalNoteEntity> notes);
    void deleteMentalNote(@Param("userId") Long userId, @Param("logDate") LocalDate logDate);
}


