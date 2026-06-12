package io.github.mrrenan.myfitnesspartner.application.service;

import io.github.mrrenan.myfitnesspartner.domain.exception.UserNotFoundException;
import io.github.mrrenan.myfitnesspartner.domain.model.*;
import io.github.mrrenan.myfitnesspartner.domain.repository.DailyGoalRepository;
import io.github.mrrenan.myfitnesspartner.domain.repository.UserRepository;
import io.github.mrrenan.myfitnesspartner.presentation.dto.DailyGoalResponse;
import io.github.mrrenan.myfitnesspartner.presentation.mapper.DailyGoalMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DailyGoalServiceImpl")
class DailyGoalServiceImplTest {

    @Mock private DailyGoalRepository dailyGoalRepository;
    @Mock private UserRepository userRepository;
    @Mock private DailyGoalMapper dailyGoalMapper;

    @InjectMocks
    private DailyGoalServiceImpl dailyGoalService;

    private User user;
    private DailyGoal dailyGoal;
    private DailyGoalResponse dailyGoalResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
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

        dailyGoal = DailyGoal.builder()
                .id(1L)
                .user(user)
                .date(LocalDate.now())
                .calorieGoal(2387)
                .caloriesConsumed(1200)
                .mealCount(3)
                .build();

        dailyGoalResponse = DailyGoalResponse.builder()
                .id(1L)
                .userId(1L)
                .calorieGoal(2387)
                .caloriesConsumed(1200)
                .remainingCalories(1187)
                .build();
    }

    @Test
    @DisplayName("deve retornar meta do dia atual")
    void getTodaysGoal_shouldReturnTodaysGoal() {
        // arrange
        when(userRepository.findByWhatsappNumberAndIsActiveTrue("+5511999999999"))
                .thenReturn(Optional.of(user));
        when(dailyGoalRepository.findByUserAndDate(user, LocalDate.now()))
                .thenReturn(Optional.of(dailyGoal));
        when(dailyGoalMapper.toResponse(dailyGoal))
                .thenReturn(dailyGoalResponse);

        // act
        DailyGoalResponse response = dailyGoalService.getTodaysGoal("+5511999999999");

        // assert
        assertThat(response).isNotNull();
        assertThat(response.getCalorieGoal()).isEqualTo(2387);
    }

    @Test
    @DisplayName("deve criar meta do dia quando não existe")
    void getTodaysGoal_shouldCreateGoal_whenNotExists() {
        // arrange
        when(userRepository.findByWhatsappNumberAndIsActiveTrue("+5511999999999"))
                .thenReturn(Optional.of(user));
        when(dailyGoalRepository.findByUserAndDate(any(), any()))
                .thenReturn(Optional.empty());
        when(dailyGoalRepository.save(any())).thenReturn(dailyGoal);
        when(dailyGoalMapper.toResponse(any())).thenReturn(dailyGoalResponse);

        // act
        DailyGoalResponse response = dailyGoalService.getTodaysGoal("+5511999999999");

        // assert
        assertThat(response).isNotNull();
        verify(dailyGoalRepository).save(any(DailyGoal.class));
    }

    @Test
    @DisplayName("deve adicionar calorias à meta do dia")
    void addCaloriesToToday_shouldUpdateCaloriesConsumed() {
        // arrange
        when(userRepository.findByWhatsappNumberAndIsActiveTrue("+5511999999999"))
                .thenReturn(Optional.of(user));
        when(dailyGoalRepository.findByUserAndDate(any(), any()))
                .thenReturn(Optional.of(dailyGoal));
        when(dailyGoalRepository.save(any())).thenReturn(dailyGoal);
        when(dailyGoalMapper.toResponse(any())).thenReturn(dailyGoalResponse);

        // act
        dailyGoalService.addCaloriesToToday("+5511999999999", 300);

        // assert
        verify(dailyGoalRepository).save(argThat(goal ->
                goal.getCaloriesConsumed() == 1500 // 1200 + 300
        ));
    }

    @Test
    @DisplayName("deve retornar histórico dos últimos N dias")
    void getLastDaysGoals_shouldReturnGoals() {
        // arrange
        when(userRepository.findByWhatsappNumberAndIsActiveTrue("+5511999999999"))
                .thenReturn(Optional.of(user));
        when(dailyGoalRepository.findByUserAndDateBetweenOrderByDateDesc(
                any(), any(), any()))
                .thenReturn(List.of(dailyGoal));
        when(dailyGoalMapper.toResponse(any())).thenReturn(dailyGoalResponse);

        // act
        List<DailyGoalResponse> goals =
                dailyGoalService.getLastDaysGoals("+5511999999999", 7);

        // assert
        assertThat(goals).hasSize(1);
    }

    @Test
    @DisplayName("deve lançar exceção quando usuário não encontrado")
    void getTodaysGoal_shouldThrowException_whenUserNotFound() {
        // arrange
        when(userRepository.findByWhatsappNumberAndIsActiveTrue(anyString()))
                .thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() ->
                dailyGoalService.getTodaysGoal("+5511999999999"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("deve resetar meta do dia")
    void resetTodaysGoal_shouldResetCalories() {
        // arrange
        when(userRepository.findByWhatsappNumberAndIsActiveTrue("+5511999999999"))
                .thenReturn(Optional.of(user));
        when(dailyGoalRepository.findByUserAndDate(any(), any()))
                .thenReturn(Optional.of(dailyGoal));
        when(dailyGoalRepository.save(any())).thenReturn(dailyGoal);
        when(dailyGoalMapper.toResponse(any())).thenReturn(dailyGoalResponse);

        // act
        dailyGoalService.resetTodaysGoal("+5511999999999");

        // assert
        verify(dailyGoalRepository).save(argThat(goal ->
                goal.getCaloriesConsumed() == 0 && goal.getMealCount() == 0
        ));
    }
}