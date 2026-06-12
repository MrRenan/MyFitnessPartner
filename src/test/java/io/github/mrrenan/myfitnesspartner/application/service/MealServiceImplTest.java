package io.github.mrrenan.myfitnesspartner.application.service;

import io.github.mrrenan.myfitnesspartner.application.port.out.FitnessAiPort;
import io.github.mrrenan.myfitnesspartner.application.dto.CalorieEstimate;
import io.github.mrrenan.myfitnesspartner.domain.exception.DailyLimitExceededException;
import io.github.mrrenan.myfitnesspartner.domain.exception.UserNotFoundException;
import io.github.mrrenan.myfitnesspartner.domain.model.*;
import io.github.mrrenan.myfitnesspartner.domain.repository.MealRepository;
import io.github.mrrenan.myfitnesspartner.domain.repository.UserRepository;
import io.github.mrrenan.myfitnesspartner.infrastructure.config.AppProperties;
import io.github.mrrenan.myfitnesspartner.presentation.dto.CreateMealFromDescriptionRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.CreateMealRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.MealResponse;
import io.github.mrrenan.myfitnesspartner.presentation.mapper.MealMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MealServiceImpl")
class MealServiceImplTest {

    @Mock private MealRepository mealRepository;
    @Mock private UserRepository userRepository;
    @Mock private DailyGoalService dailyGoalService;
    @Mock private FitnessAiPort fitnessAiPort;
    @Mock private MealMapper mealMapper;
    @Mock private AppProperties appProperties;
    @Mock private AppProperties.Fitness fitnessProperties;

    @InjectMocks
    private MealServiceImpl mealService;

    private User user;
    private Meal meal;
    private MealResponse mealResponse;

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

        meal = Meal.builder()
                .id(1L)
                .user(user)
                .description("Frango grelhado")
                .mealType(MealType.LUNCH)
                .calories(350)
                .mealDate(LocalDateTime.now())
                .build();

        mealResponse = MealResponse.builder()
                .id(1L)
                .userId(1L)
                .description("Frango grelhado")
                .calories(350)
                .build();

        lenient().when(appProperties.getFitness()).thenReturn(fitnessProperties);
        lenient().when(fitnessProperties.getMaxDailyMeals()).thenReturn(10);
    }

    @Test
    @DisplayName("deve registrar refeição e atualizar meta diária")
    void registerMeal_shouldSaveMealAndUpdateDailyGoal() {
        // arrange
        CreateMealRequest request = CreateMealRequest.builder()
                .whatsappNumber("+5511999999999")
                .description("Frango grelhado")
                .mealType(MealType.LUNCH)
                .calories(350)
                .build();

        when(userRepository.findByWhatsappNumberAndIsActiveTrue("+5511999999999"))
                .thenReturn(Optional.of(user));
        when(mealRepository.countMealsByUserAndDate(any(), any(), any()))
                .thenReturn(3L);
        when(mealMapper.toEntity(any(), any())).thenReturn(meal);
        when(mealRepository.save(any())).thenReturn(meal);
        when(mealMapper.toResponse(any())).thenReturn(mealResponse);

        // act
        MealResponse response = mealService.registerMeal(request);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.getCalories()).isEqualTo(350);
        verify(mealRepository).save(any());
        verify(dailyGoalService).addCaloriesToToday("+5511999999999", 350);
    }

    @Test
    @DisplayName("deve lançar exceção quando calorias não informadas")
    void registerMeal_shouldThrowException_whenCaloriesNotProvided() {
        // arrange — sem stubs, exceção lança antes de qualquer chamada
        CreateMealRequest request = CreateMealRequest.builder()
                .whatsappNumber("+5511999999999")
                .description("Frango grelhado")
                .mealType(MealType.LUNCH)
                .calories(null)
                .build();

        // act & assert
        assertThatThrownBy(() -> mealService.registerMeal(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(mealRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar exceção quando limite diário de refeições excedido")
    void registerMeal_shouldThrowException_whenDailyLimitExceeded() {
        // arrange
        CreateMealRequest request = CreateMealRequest.builder()
                .whatsappNumber("+5511999999999")
                .description("Frango grelhado")
                .mealType(MealType.LUNCH)
                .calories(350)
                .build();

        when(userRepository.findByWhatsappNumberAndIsActiveTrue("+5511999999999"))
                .thenReturn(Optional.of(user));
        when(mealRepository.countMealsByUserAndDate(any(), any(), any()))
                .thenReturn(10L); // atingiu o limite

        // act & assert
        assertThatThrownBy(() -> mealService.registerMeal(request))
                .isInstanceOf(DailyLimitExceededException.class);

        verify(mealRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve lançar exceção quando usuário não encontrado")
    void registerMeal_shouldThrowException_whenUserNotFound() {
        // arrange
        CreateMealRequest request = CreateMealRequest.builder()
                .whatsappNumber("+5511999999999")
                .description("Frango grelhado")
                .mealType(MealType.LUNCH)
                .calories(350)
                .build();

        when(userRepository.findByWhatsappNumberAndIsActiveTrue(anyString()))
                .thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> mealService.registerMeal(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("deve registrar refeição a partir de descrição usando IA")
    void registerMealFromDescription_shouldUseAiToCalculateCalories() {
        // arrange
        CreateMealFromDescriptionRequest request =
                CreateMealFromDescriptionRequest.builder()
                        .whatsappNumber("+5511999999999")
                        .description("200g frango grelhado com arroz")
                        .mealType(MealType.LUNCH)
                        .build();

        CalorieEstimate estimate = CalorieEstimate.builder()
                .calories(450)
                .protein(35.0)
                .carbohydrates(40.0)
                .fat(10.0)
                .explanation("Frango grelhado com arroz integral")
                .confidence(0.9)
                .build();

        when(fitnessAiPort.analyzeFood(anyString())).thenReturn(estimate);
        when(userRepository.findByWhatsappNumberAndIsActiveTrue("+5511999999999"))
                .thenReturn(Optional.of(user));
        when(mealRepository.countMealsByUserAndDate(any(), any(), any()))
                .thenReturn(3L);
        when(mealMapper.toEntity(any(), any())).thenReturn(meal);
        when(mealRepository.save(any())).thenReturn(meal);
        when(mealMapper.toResponse(any())).thenReturn(mealResponse);

        // act
        MealResponse response = mealService.registerMealFromDescription(request);

        // assert
        assertThat(response).isNotNull();
        verify(fitnessAiPort).analyzeFood("200g frango grelhado com arroz");
    }

    @Test
    @DisplayName("deve retornar todas as refeições do usuário")
    void getAllMeals_shouldReturnAllMeals() {
        // arrange
        when(userRepository.findByWhatsappNumberAndIsActiveTrue("+5511999999999"))
                .thenReturn(Optional.of(user));
        when(mealRepository.findByUserOrderByMealDateDesc(user))
                .thenReturn(List.of(meal));
        when(mealMapper.toResponse(any())).thenReturn(mealResponse);

        // act
        List<MealResponse> meals = mealService.getAllMeals("+5511999999999");

        // assert
        assertThat(meals).hasSize(1);
        assertThat(meals.get(0).getDescription()).isEqualTo("Frango grelhado");
    }
}