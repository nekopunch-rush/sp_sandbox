package nekopunch_rush.testjava21.infrastructure.repository.mapper;

import nekopunch_rush.testjava21.infrastructure.repository.entity.ActivityEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.BodyMetricsEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.MealEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.MentalNoteEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface RelatedEntitiesMapper {

    List<BodyMetricsEntity> selectBodyMetrics(@Param("userId") Long userId, @Param("logDate") LocalDate logDate);

    List<ActivityEntity> selectActivity(@Param("userId") Long userId, @Param("logDate") LocalDate logDate);

    List<MealEntity> selectMeal(@Param("userId") Long userId, @Param("logDate") LocalDate logDate);

    List<MentalNoteEntity> selectMentalNote(@Param("userId") Long userId, @Param("logDate") LocalDate logDate);
}
