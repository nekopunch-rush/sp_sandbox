package nekopunch_rush.testjava21.infrastructure.repository.mapper;

import nekopunch_rush.testjava21.infrastructure.repository.entity.BodyMetricsEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.ActivityEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.MealEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.MentalNoteEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@MybatisTest
class RelatedEntitiesMapperTest {

    @Container
    static MySQLContainer<?> dbContainer = new MySQLContainer<>("mysql:8")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", dbContainer::getJdbcUrl);
        registry.add("spring.datasource.username", dbContainer::getUsername);
        registry.add("spring.datasource.password", dbContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", dbContainer::getDriverClassName);
        registry.add("spring.datasource.port", dbContainer::getFirstMappedPort);
    }

    @Autowired
    private RelatedEntitiesMapper relatedEntitiesMapper;

    @Autowired
    private DailyLogMapper dailyLogMapper;

    @Test
    @DisplayName("selectBodyMetricsの正常系")
    void selectBodyMetricsNormal() {
        BodyMetricsEntity metrics = BodyMetricsEntity.builder()
                .userId(100L)
                .logDate(LocalDate.of(2030, 1, 1))
                .weight(70.0)
                .bodyFatPercentage(20.0)
                .muscleMass(50.0)
                .measuredAt(LocalDateTime.of(2030, 1, 1, 7, 0))
                .build();
        dailyLogMapper.insertBodyMetrics(Collections.singletonList(metrics));
        List<BodyMetricsEntity> result = relatedEntitiesMapper.selectBodyMetrics(100L, LocalDate.of(2030, 1, 1));
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getWeight()).isEqualTo(70.0);
    }

    @Test
    @DisplayName("selectBodyMetricsの異常系（データなし）")
    void selectBodyMetricsAbnormalNotFound() {
        List<BodyMetricsEntity> result = relatedEntitiesMapper.selectBodyMetrics(999L, LocalDate.of(2099, 1, 1));
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("selectActivityの正常系")
    void selectActivityNormal() {
        ActivityEntity activity = ActivityEntity.builder()
                .userId(101L)
                .logDate(LocalDate.of(2030, 2, 2))
                .type("walk")
                .startedAt(LocalDateTime.of(2030, 2, 2, 8, 0))
                .durationInMinutes(60)
                .distanceKm(4.0)
                .steps(8000)
                .caloriesBurned(200)
                .build();
        dailyLogMapper.insertActivity(Collections.singletonList(activity));
        List<ActivityEntity> result = relatedEntitiesMapper.selectActivity(101L, LocalDate.of(2030, 2, 2));
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getType()).isEqualTo("walk");
    }

    @Test
    @DisplayName("selectActivityの異常系（データなし）")
    void selectActivityAbnormalNotFound() {
        List<ActivityEntity> result = relatedEntitiesMapper.selectActivity(999L, LocalDate.of(2099, 2, 2));
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("selectMealの正常系")
    void selectMealNormal() {
        MealEntity meal = MealEntity.builder()
                .userId(102L)
                .logDate(LocalDate.of(2030, 3, 3))
                .time(LocalTime.of(12, 0))
                .content("昼食: カレー")
                .calories(700)
                .photoUrl("http://example.com/lunch.jpg")
                .build();
        dailyLogMapper.insertMeal(Collections.singletonList(meal));
        List<MealEntity> result = relatedEntitiesMapper.selectMeal(102L, LocalDate.of(2030, 3, 3));
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getContent()).isEqualTo("昼食: カレー");
    }

    @Test
    @DisplayName("selectMealの異常系（データなし）")
    void selectMealAbnormalNotFound() {
        List<MealEntity> result = relatedEntitiesMapper.selectMeal(999L, LocalDate.of(2099, 3, 3));
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("selectMentalNoteの正常系")
    void selectMentalNoteNormal() {
        MentalNoteEntity note = MentalNoteEntity.builder()
                .userId(103L)
                .logDate(LocalDate.of(2030, 4, 4))
                .recordedAt(LocalDateTime.of(2030, 4, 4, 22, 0))
                .stressLevel(1)
                .motivationLevel(5)
                .note("最高の一日")
                .build();
        dailyLogMapper.insertMentalNote(Collections.singletonList(note));
        List<MentalNoteEntity> result = relatedEntitiesMapper.selectMentalNote(103L, LocalDate.of(2030, 4, 4));
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getNote()).isEqualTo("最高の一日");
    }

    @Test
    @DisplayName("selectMentalNoteの異常系（データなし）")
    void selectMentalNoteAbnormalNotFound() {
        List<MentalNoteEntity> result = relatedEntitiesMapper.selectMentalNote(999L, LocalDate.of(2099, 4, 4));
        assertThat(result).isEmpty();
    }
}
