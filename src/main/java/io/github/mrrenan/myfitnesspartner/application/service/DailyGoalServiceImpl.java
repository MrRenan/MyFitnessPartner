package io.github.mrrenan.myfitnesspartner.application.service;

import io.github.mrrenan.myfitnesspartner.domain.exception.UserNotFoundException;
import io.github.mrrenan.myfitnesspartner.domain.model.DailyGoal;
import io.github.mrrenan.myfitnesspartner.domain.model.User;
import io.github.mrrenan.myfitnesspartner.domain.repository.DailyGoalRepository;
import io.github.mrrenan.myfitnesspartner.domain.repository.UserRepository;
import io.github.mrrenan.myfitnesspartner.presentation.dto.DailyGoalResponse;
import io.github.mrrenan.myfitnesspartner.presentation.mapper.DailyGoalMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of DailyGoalService.
 * Handles daily calorie tracking and progress monitoring.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyGoalServiceImpl implements DailyGoalService {

    private final DailyGoalRepository dailyGoalRepository;
    private final UserRepository userRepository;
    private final DailyGoalMapper dailyGoalMapper;

    @Override
    @Transactional
    public DailyGoalResponse getTodaysGoal(String whatsappNumber) {
        log.debug("Getting today's goal for user: {}", whatsappNumber);
        return getGoalByDate(whatsappNumber, LocalDate.now());
    }

    @Override
    @Transactional
    public DailyGoalResponse getGoalByDate(String whatsappNumber, LocalDate date) {
        log.debug("Getting goal for user {} on date {}", whatsappNumber, date);

        User user = findUserByWhatsapp(whatsappNumber);

        // Try to find existing goal for the date
        DailyGoal dailyGoal = dailyGoalRepository.findByUserAndDate(user, date)
                .orElseGet(() -> {
                    log.info("Creating new daily goal for user {} on {}", whatsappNumber, date);
                    return createDailyGoal(user, date);
                });

        return dailyGoalMapper.toResponse(dailyGoal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyGoalResponse> getLastDaysGoals(String whatsappNumber, int days) {
        log.debug("Getting last {} days goals for user: {}", days, whatsappNumber);

        User user = findUserByWhatsapp(whatsappNumber);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<DailyGoal> goals = dailyGoalRepository.findByUserAndDateBetweenOrderByDateDesc(
                user, startDate, endDate
        );

        return goals.stream()
                .map(dailyGoalMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DailyGoalResponse addCaloriesToToday(String whatsappNumber, int calories) {
        log.info("Adding {} calories to today's goal for user: {}", calories, whatsappNumber);

        User user = findUserByWhatsapp(whatsappNumber);
        LocalDate today = LocalDate.now();

        // Get or create today's goal
        DailyGoal dailyGoal = dailyGoalRepository.findByUserAndDate(user, today)
                .orElseGet(() -> createDailyGoal(user, today));

        // Add calories
        dailyGoal.addMeal(calories);

        // Save updated goal
        DailyGoal updatedGoal = dailyGoalRepository.save(dailyGoal);

        log.info("Updated goal for {}: {}/{} calories consumed ({}% complete)",
                whatsappNumber,
                updatedGoal.getCaloriesConsumed(),
                updatedGoal.getCalorieGoal(),
                String.format("%.1f", updatedGoal.getProgressPercentage()));

        return dailyGoalMapper.toResponse(updatedGoal);
    }

    @Override
    @Transactional
    public DailyGoalResponse resetTodaysGoal(String whatsappNumber) {
        log.info("Resetting today's goal for user: {}", whatsappNumber);

        User user = findUserByWhatsapp(whatsappNumber);
        LocalDate today = LocalDate.now();

        DailyGoal dailyGoal = dailyGoalRepository.findByUserAndDate(user, today)
                .orElseThrow(() -> new IllegalStateException("No goal found for today"));

        // Reset to zero
        dailyGoal.setCaloriesConsumed(0);
        dailyGoal.setMealCount(0);

        DailyGoal resetGoal = dailyGoalRepository.save(dailyGoal);

        log.info("Goal reset for user: {}", whatsappNumber);

        return dailyGoalMapper.toResponse(resetGoal);
    }

    /**
     * Create a new daily goal for a user on a specific date
     */
    private DailyGoal createDailyGoal(User user, LocalDate date) {
        DailyGoal dailyGoal = DailyGoal.builder()
                .user(user)
                .date(date)
                .calorieGoal(user.getDailyCalorieGoal())
                .caloriesConsumed(0)
                .mealCount(0)
                .build();

        return dailyGoalRepository.save(dailyGoal);
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