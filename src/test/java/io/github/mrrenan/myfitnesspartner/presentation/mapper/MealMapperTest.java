package io.github.mrrenan.myfitnesspartner.presentation.mapper;

import io.github.mrrenan.myfitnesspartner.domain.model.*;
import io.github.mrrenan.myfitnesspartner.presentation.dto.CreateMealRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.MealResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MealMapper")
class MealMapperTest {

    private final MealMapper mealMapper = new MealMapper();

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
    @DisplayName("deve converter CreateMealRequest para Meal")
    void toEntity_shouldMapCorrectly() {
        User user = buildUser();
        CreateMealRequest request = CreateMealRequest.builder()
                .description("Frango grelhado")
                .mealType(MealType.LUNCH)
                .calories(350)
                .protein(28.5)
                .carbohydrates(30.0)
                .fat(12.0)
                .build();

        Meal meal = mealMapper.toEntity(request, user);

        assertThat(meal.getDescription()).isEqualTo("Frango grelhado");
        assertThat(meal.getMealType()).isEqualTo(MealType.LUNCH);
        assertThat(meal.getCalories()).isEqualTo(350);
        assertThat(meal.getProtein()).isEqualTo(28.5);
        assertThat(meal.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("deve usar data atual quando mealDate não informado")
    void toEntity_shouldUseCurrentDate_whenMealDateIsNull() {
        User user = buildUser();
        CreateMealRequest request = CreateMealRequest.builder()
                .description("Frango grelhado")
                .mealType(MealType.LUNCH)
                .calories(350)
                .build();

        Meal meal = mealMapper.toEntity(request, user);

        assertThat(meal.getMealDate()).isNotNull();
        assertThat(meal.getMealDate()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("deve converter Meal para MealResponse")
    void toResponse_shouldMapCorrectly() {
        User user = buildUser();
        Meal meal = Meal.builder()
                .id(1L)
                .user(user)
                .description("Frango grelhado")
                .mealType(MealType.LUNCH)
                .calories(350)
                .protein(28.5)
                .mealDate(LocalDateTime.now())
                .build();

        MealResponse response = mealMapper.toResponse(meal);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getDescription()).isEqualTo("Frango grelhado");
        assertThat(response.getCalories()).isEqualTo(350);
        assertThat(response.getProtein()).isEqualTo(28.5);
    }
}