package nekopunch_rush.testjava21.infrastructure.repository.mapper;

import nekopunch_rush.testjava21.infrastructure.repository.entity.DailyLogEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.BodyMetricsEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.ActivityEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.MealEntity;
import nekopunch_rush.testjava21.infrastructure.repository.entity.MentalNoteEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@MybatisTest
class DailyLogMapperTest {

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
    private DailyLogMapper dailyLogMapper;

    @Test
    @DisplayName("insertとfindByIdの正常系")
    void insertAndFindById() {
        DailyLogEntity log = DailyLogEntity.builder()
                .userId(1L)
                .logDate(LocalDate.of(2025, 5, 1))
                .sleepHours(7.5)
                .moodLevel(3)
                .build();

        dailyLogMapper.insert(log);
        DailyLogEntity result = dailyLogMapper.findByUserIdAndLogDate(1L, LocalDate.of(2025, 5, 1));

        assertThat(result).isNotNull();
        assertThat(result.getSleepHours()).isEqualTo(7.5);
        assertThat(result.getMoodLevel()).isEqualTo(3);
    }

    @Test
    @DisplayName("updateの正常系")
    void updateNormal() {
        DailyLogEntity log = DailyLogEntity.builder()
                .userId(2L)
                .logDate(LocalDate.of(2025, 6, 1))
                .sleepHours(6.0)
                .moodLevel(2)
                .build();
        dailyLogMapper.insert(log);

        DailyLogEntity updated = DailyLogEntity.builder()
                .userId(2L)
                .logDate(LocalDate.of(2025, 6, 1))
                .sleepHours(8.0)
                .moodLevel(5)
                .build();
        dailyLogMapper.update(2L, LocalDate.of(2025, 6, 1), updated);

        DailyLogEntity result = dailyLogMapper.findByUserIdAndLogDate(2L, LocalDate.of(2025, 6, 1));
        assertThat(result).isNotNull();
        assertThat(result.getSleepHours()).isEqualTo(8.0);
        assertThat(result.getMoodLevel()).isEqualTo(5);
    }

    @Test
    @DisplayName("updateの異常系（存在しないデータ）")
    void updateAbnormalNotFound() {
        DailyLogEntity updated = DailyLogEntity.builder()
                .userId(999L)
                .logDate(LocalDate.of(2099, 1, 1))
                .sleepHours(5.0)
                .moodLevel(1)
                .build();
        // 存在しないデータのupdateは例外は出ないが、更新もされない
        dailyLogMapper.update(999L, LocalDate.of(2099, 1, 1), updated);
        DailyLogEntity result = dailyLogMapper.findByUserIdAndLogDate(999L, LocalDate.of(2099, 1, 1));
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("deleteの正常系")
    void deleteNormal() {
        DailyLogEntity log = DailyLogEntity.builder()
                .userId(3L)
                .logDate(LocalDate.of(2025, 7, 1))
                .sleepHours(7.0)
                .moodLevel(4)
                .build();
        dailyLogMapper.insert(log);

        dailyLogMapper.delete(3L, LocalDate.of(2025, 7, 1));
        DailyLogEntity result = dailyLogMapper.findByUserIdAndLogDate(3L, LocalDate.of(2025, 7, 1));
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("deleteの異常系（存在しないデータ）")
    void deleteAbnormalNotFound() {
        // 存在しないデータのdeleteは例外は出ない
        dailyLogMapper.delete(999L, LocalDate.of(2099, 1, 1));
        // 削除後もデータが無いことを確認
        DailyLogEntity result = dailyLogMapper.findByUserIdAndLogDate(999L, LocalDate.of(2099, 1, 1));
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("insertの異常系（必須項目null）")
    void insertAbnormalNullRequired() {
        DailyLogEntity log = DailyLogEntity.builder()
                .userId(null)
                .logDate(LocalDate.of(2025, 8, 1))
                .sleepHours(7.0)
                .moodLevel(3)
                .build();
        assertThatThrownBy(() -> dailyLogMapper.insert(log))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("findByUserIdAndLogDateの異常系（存在しないデータ）")
    void findByUserIdAndLogDateAbnormalNotFound() {
        DailyLogEntity result = dailyLogMapper.findByUserIdAndLogDate(888L, LocalDate.of(2099, 12, 31));
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("insertBodyMetricsとdeleteBodyMetricsの正常系")
    void insertAndDeleteBodyMetrics() {
        BodyMetricsEntity metrics = BodyMetricsEntity.builder()
                .userId(10L)
                .logDate(LocalDate.of(2025, 9, 1))
                .weight(60.5)
                .bodyFatPercentage(18.2)
                .muscleMass(45.0)
                .measuredAt(LocalDateTime.of(2025, 9, 1, 7, 0))
                .build();
        dailyLogMapper.insertBodyMetrics(Collections.singletonList(metrics));
        // 削除しても例外が出ないことを確認
        dailyLogMapper.deleteBodyMetrics(10L, LocalDate.of(2025, 9, 1));
        // 削除後の確認はselectが無いので例外が出ないことのみ確認
    }

    @Test
    @DisplayName("insertActivityとdeleteActivityの正常系")
    void insertAndDeleteActivity() {
        ActivityEntity activity = ActivityEntity.builder()
                .userId(11L)
                .logDate(LocalDate.of(2025, 9, 2))
                .type("run")
                .startedAt(LocalDateTime.of(2025, 9, 2, 6, 0))
                .durationInMinutes(30)
                .distanceKm(5.0)
                .steps(6000)
                .caloriesBurned(300)
                .build();
        dailyLogMapper.insertActivity(Collections.singletonList(activity));
        dailyLogMapper.deleteActivity(11L, LocalDate.of(2025, 9, 2));
    }

    @Test
    @DisplayName("insertMealとdeleteMealの正常系")
    void insertAndDeleteMeal() {
        MealEntity meal = MealEntity.builder()
                .userId(12L)
                .logDate(LocalDate.of(2025, 9, 3))
                .time(LocalTime.of(8, 0))
                .content("朝食: トーストと卵")
                .calories(400)
                .photoUrl("http://example.com/photo.jpg")
                .build();
        dailyLogMapper.insertMeal(Collections.singletonList(meal));
        dailyLogMapper.deleteMeal(12L, LocalDate.of(2025, 9, 3));
    }

    @Test
    @DisplayName("insertMentalNoteとdeleteMentalNoteの正常系")
    void insertAndDeleteMentalNote() {
        MentalNoteEntity note = MentalNoteEntity.builder()
                .userId(13L)
                .logDate(LocalDate.of(2025, 9, 4))
                .recordedAt(LocalDateTime.of(2025, 9, 4, 21, 0))
                .stressLevel(2)
                .motivationLevel(4)
                .note("今日は調子が良い")
                .build();
        dailyLogMapper.insertMentalNote(Collections.singletonList(note));
        dailyLogMapper.deleteMentalNote(13L, LocalDate.of(2025, 9, 4));
    }
}
