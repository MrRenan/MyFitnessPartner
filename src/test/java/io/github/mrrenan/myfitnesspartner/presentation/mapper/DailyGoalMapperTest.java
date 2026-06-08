package io.github.mrrenan.myfitnesspartner.presentation.mapper;

import io.github.mrrenan.myfitnesspartner.domain.model.*;
import io.github.mrrenan.myfitnesspartner.presentation.dto.DailyGoalResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DailyGoalMapper")
class DailyGoalMapperTest {

    private final DailyGoalMapper dailyGoalMapper = new DailyGoalMapper();

    private User buildUser() {
        return User.builder()
                .id(1L)
                .name("Renan")
                .whatsappNumber("+5511999999999")
                .password("senha")
                .dateOfBirth(LocalDate.of(1994, 2, 18))
                .gender(Gender.MALE)
                .weight(93.0)
                .height(174.0)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .goalType(GoalType.LOSE_WEIGHT)
                .dailyCalorieGoal(2387)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("deve converter DailyGoal para DailyGoalResponse")
    void toResponse_shouldMapCorrectly() {
        // arrange
        User user = buildUser();
        DailyGoal goal = DailyGoal.builder()
                .id(1L)
                .user(user)
                .date(LocalDate.now())
                .calorieGoal(2387)
                .caloriesConsumed(1200)
                .mealCount(3)
                .build();

        // act
        DailyGoalResponse response = dailyGoalMapper.toResponse(goal);

        // assert
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getCalorieGoal()).isEqualTo(2387);
        assertThat(response.getCaloriesConsumed()).isEqualTo(1200);
        assertThat(response.getRemainingCalories()).isEqualTo(1187);
        assertThat(response.getMealCount()).isEqualTo(3);
        assertThat(response.getStatus()).isEqualTo("Under Goal");
    }

    @Test
    @DisplayName("deve retornar status On Track quando progresso entre 80% e 110%")
    void toResponse_shouldReturnOnTrack_whenProgressIsGood() {
        // arrange
        User user = buildUser();
        DailyGoal goal = DailyGoal.builder()
                .id(1L)
                .user(user)
                .date(LocalDate.now())
                .calorieGoal(2000)
                .caloriesConsumed(1800)
                .mealCount(4)
                .build();

        // act
        DailyGoalResponse response = dailyGoalMapper.toResponse(goal);

        // assert
        assertThat(response.getStatus()).isEqualTo("On Track");
    }

    @Test
    @DisplayName("deve retornar status Over Goal quando ultrapassar 110%")
    void toResponse_shouldReturnOverGoal_whenExceeded() {
        // arrange
        User user = buildUser();
        DailyGoal goal = DailyGoal.builder()
                .id(1L)
                .user(user)
                .date(LocalDate.now())
                .calorieGoal(2000)
                .caloriesConsumed(2400)
                .mealCount(6)
                .build();

        // act
        DailyGoalResponse response = dailyGoalMapper.toResponse(goal);

        // assert
        assertThat(response.getStatus()).isEqualTo("Over Goal");
    }
}