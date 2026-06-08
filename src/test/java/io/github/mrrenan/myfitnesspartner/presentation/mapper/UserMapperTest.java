package io.github.mrrenan.myfitnesspartner.presentation.mapper;

import io.github.mrrenan.myfitnesspartner.domain.model.ActivityLevel;
import io.github.mrrenan.myfitnesspartner.domain.model.Gender;
import io.github.mrrenan.myfitnesspartner.domain.model.GoalType;
import io.github.mrrenan.myfitnesspartner.domain.model.User;
import io.github.mrrenan.myfitnesspartner.presentation.dto.UpdateUserRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserMapper")
class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

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
    @DisplayName("deve converter User para UserResponse com campos calculados")
    void toResponse_shouldMapWithCalculatedFields() {
        // arrange
        User user = buildUser();

        // act
        UserResponse response = userMapper.toResponse(user);

        // assert
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Renan");
        assertThat(response.getAge()).isPositive();
        assertThat(response.getBmr()).isPositive();
        assertThat(response.getTdee()).isPositive();
        assertThat(response.getDailyCalorieGoal()).isEqualTo(2387);
    }

    @Test
    @DisplayName("deve atualizar apenas campos não nulos no updateEntity")
    void updateEntity_shouldUpdateOnlyNonNullFields() {
        // arrange
        User user = buildUser();
        UpdateUserRequest request = UpdateUserRequest.builder()
                .weight(90.0)
                .build();

        // act
        userMapper.updateEntity(user, request);

        // assert
        assertThat(user.getWeight()).isEqualTo(90.0);
        assertThat(user.getName()).isEqualTo("Renan"); // não mudou
        assertThat(user.getHeight()).isEqualTo(174.0); // não mudou
    }
}