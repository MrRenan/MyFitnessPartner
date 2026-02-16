package io.github.mrrenan.myfitnesspartner.application.service;

import io.github.mrrenan.myfitnesspartner.domain.exception.DailyLimitExceededException;
import io.github.mrrenan.myfitnesspartner.domain.exception.UserNotFoundException;
import io.github.mrrenan.myfitnesspartner.domain.model.Meal;
import io.github.mrrenan.myfitnesspartner.domain.model.User;
import io.github.mrrenan.myfitnesspartner.domain.repository.MealRepository;
import io.github.mrrenan.myfitnesspartner.domain.repository.UserRepository;
import io.github.mrrenan.myfitnesspartner.infrastructure.config.AppProperties;
import io.github.mrrenan.myfitnesspartner.infrastructure.util.DateUtils;
import io.github.mrrenan.myfitnesspartner.presentation.dto.CreateMealFromDescriptionRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.CreateMealRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.MealResponse;
import io.github.mrrenan.myfitnesspartner.presentation.mapper.MealMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of MealService.
 * Handles meal registration and automatically updates daily goals.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MealServiceImpl implements MealService {

    private final MealRepository mealRepository;
    private final UserRepository userRepository;
    private final DailyGoalService dailyGoalService;
    private final MealMapper mealMapper;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public MealResponse registerMeal(CreateMealRequest request) {
        log.info("Registering meal for user: {} - {} calories",
                request.getWhatsappNumber(),
                request.getCalories() != null ? request.getCalories() : "AI calculation pending");

        // Validate that calories are provided
        // When AI integration is ready, this will be calculated automatically
        if (request.getCalories() == null) {
            throw new IllegalArgumentException(
                    "Calories must be provided. In the future, use AI service to calculate from description."
            );
        }

        User user = findUserByWhatsapp(request.getWhatsappNumber());

        // Check daily meal limit
        Long todaysMealCount = mealRepository.countMealsByUserAndDate(
                user,
                DateUtils.getStartOfToday(),
                DateUtils.getTomorrow()
        );

        Integer maxMeals = appProperties.getFitness().getMaxDailyMeals();
        if (todaysMealCount >= maxMeals) {
            log.warn("User {} exceeded daily meal limit: {}/{}",
                    request.getWhatsappNumber(), todaysMealCount, maxMeals);
            throw new DailyLimitExceededException(maxMeals);
        }

        // Create and save meal
        Meal meal = mealMapper.toEntity(request, user);
        Meal savedMeal = mealRepository.save(meal);

        log.info("Meal saved with ID: {} for user: {}", savedMeal.getId(), user.getId());

        // IMPORTANT: Automatically update daily goal
        dailyGoalService.addCaloriesToToday(request.getWhatsappNumber(), request.getCalories());

        log.info("Daily goal updated with {} calories for user: {}",
                request.getCalories(), request.getWhatsappNumber());

        return mealMapper.toResponse(savedMeal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MealResponse> getAllMeals(String whatsappNumber) {
        log.debug("Getting all meals for user: {}", whatsappNumber);

        User user = findUserByWhatsapp(whatsappNumber);
        List<Meal> meals = mealRepository.findByUserOrderByMealDateDesc(user);

        return meals.stream()
                .map(mealMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MealResponse> getTodaysMeals(String whatsappNumber) {
        log.debug("Getting today's meals for user: {}", whatsappNumber);

        User user = findUserByWhatsapp(whatsappNumber);
        List<Meal> meals = mealRepository.findTodaysMealsByUser(
                user,
                DateUtils.getStartOfToday(),
                DateUtils.getTomorrow()
        );

        return meals.stream()
                .map(mealMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MealResponse> getMealsByDate(String whatsappNumber, LocalDate date) {
        log.debug("Getting meals for user {} on date {}", whatsappNumber, date);

        User user = findUserByWhatsapp(whatsappNumber);
        LocalDateTime startOfDay = DateUtils.getStartOfDay(date);
        LocalDateTime endOfDay = DateUtils.getEndOfDay(date);

        List<Meal> meals = mealRepository.findByUserAndMealDateBetweenOrderByMealDateDesc(
                user, startOfDay, endOfDay
        );

        return meals.stream()
                .map(mealMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MealResponse> getMealsByDateRange(String whatsappNumber, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting meals for user {} from {} to {}", whatsappNumber, startDate, endDate);

        User user = findUserByWhatsapp(whatsappNumber);
        LocalDateTime startDateTime = DateUtils.getStartOfDay(startDate);
        LocalDateTime endDateTime = DateUtils.getEndOfDay(endDate);

        List<Meal> meals = mealRepository.findByUserAndMealDateBetweenOrderByMealDateDesc(
                user, startDateTime, endDateTime
        );

        return meals.stream()
                .map(mealMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MealResponse getMealById(Long mealId, String whatsappNumber) {
        log.debug("Getting meal {} for user {}", mealId, whatsappNumber);

        User user = findUserByWhatsapp(whatsappNumber);
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new IllegalArgumentException("Meal not found with ID: " + mealId));

        // Verify meal belongs to the user
        if (!meal.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Meal does not belong to this user");
        }

        return mealMapper.toResponse(meal);
    }

    @Override
    @Transactional
    public void deleteMeal(Long mealId, String whatsappNumber) {
        log.info("Deleting meal {} for user {}", mealId, whatsappNumber);

        User user = findUserByWhatsapp(whatsappNumber);
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new IllegalArgumentException("Meal not found with ID: " + mealId));

        // Verify meal belongs to the user
        if (!meal.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Meal does not belong to this user");
        }

        // TODO: In future, subtract calories from daily goal if meal is from today

        mealRepository.delete(meal);
        log.info("Meal {} deleted successfully", mealId);
    }

    @Override
    @Transactional
    public MealResponse registerMealFromDescription(CreateMealFromDescriptionRequest request) {
        log.info("Attempting to register meal from description for user: {}", request.getWhatsappNumber());

        // TODO: This will be implemented when GeminiService is ready
        // For now, throw exception to indicate AI integration is needed
        throw new UnsupportedOperationException(
                "AI integration not yet implemented. " +
                        "This endpoint will use Gemini AI to calculate calories from description. " +
                        "For now, use POST /api/meals with calories already calculated."
        );

        /*
         * Future implementation:
         *
         * 1. Call GeminiService to analyze description
         * CalorieEstimate estimate = geminiService.calculateCaloriesFromDescription(request.getDescription());
         *
         * 2. Build CreateMealRequest with AI-calculated values
         * CreateMealRequest mealRequest = CreateMealRequest.builder()
         *     .whatsappNumber(request.getWhatsappNumber())
         *     .description(request.getDescription())
         *     .mealType(request.getMealType())
         *     .calories(estimate.getCalories())
         *     .protein(estimate.getProtein())
         *     .carbohydrates(estimate.getCarbohydrates())
         *     .fat(estimate.getFat())
         *     .notes(request.getNotes())
         *     .build();
         *
         * 3. Register meal with calculated values
         * return registerMeal(mealRequest);
         */
    }

    /**
     * Find user by WhatsApp number or throw exception
     */
    private User findUserByWhatsapp(String whatsappNumber) {
        return userRepository.findByWhatsappNumberAndIsActiveTrue(whatsappNumber)
                .orElseThrow(() -> {
                    log.warn("User not found with WhatsApp: {}", whatsappNumber);
                    return new UserNotFoundException(whatsappNumber);
                });
    }
}