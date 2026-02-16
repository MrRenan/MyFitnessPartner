package io.github.mrrenan.myfitnesspartner.application.service;

import io.github.mrrenan.myfitnesspartner.presentation.dto.CreateMealFromDescriptionRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.CreateMealRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.MealResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for meal management.
 * Handles meal registration and automatically updates daily goals.
 */
public interface MealService {

    /**
     * Register a new meal with pre-calculated values
     * Automatically updates the user's daily goal with consumed calories
     *
     * @param request meal data with calories already calculated
     * @return created meal response
     //* @throws UserNotFoundException if user not found
     //* @throws DailyLimitExceededException if daily meal limit exceeded
     * @throws IllegalArgumentException if calories not provided
     */
    MealResponse registerMeal(CreateMealRequest request);

    /**
     * Register a meal from natural language description
     * This method will use AI to calculate calories automatically
     *
     * Example: "200g chicken breast with rice and salad"
     *
     * @param request meal description request
     * @return created meal response with AI-calculated values
     //* @throws UserNotFoundException if user not found
     //* @throws DailyLimitExceededException if daily meal limit exceeded
     *
     * NOTE: This method will be implemented when AI integration is ready
     */
    MealResponse registerMealFromDescription(CreateMealFromDescriptionRequest request);

    /**
     * Get all meals for a user
     *
     * @param whatsappNumber user's WhatsApp number
     * @return list of all meals
     */
    List<MealResponse> getAllMeals(String whatsappNumber);

    /**
     * Get today's meals for a user
     *
     * @param whatsappNumber user's WhatsApp number
     * @return list of today's meals
     */
    List<MealResponse> getTodaysMeals(String whatsappNumber);

    /**
     * Get meals for a specific date
     *
     * @param whatsappNumber user's WhatsApp number
     * @param date specific date
     * @return list of meals for that date
     */
    List<MealResponse> getMealsByDate(String whatsappNumber, LocalDate date);

    /**
     * Get meals within a date range
     *
     * @param whatsappNumber user's WhatsApp number
     * @param startDate start date
     * @param endDate end date
     * @return list of meals in range
     */
    List<MealResponse> getMealsByDateRange(String whatsappNumber, LocalDate startDate, LocalDate endDate);

    /**
     * Get a specific meal by ID
     *
     * @param mealId meal ID
     * @param whatsappNumber user's WhatsApp number
     * @return meal response
     */
    MealResponse getMealById(Long mealId, String whatsappNumber);

    /**
     * Delete a meal
     * Also removes calories from daily goal
     *
     * @param mealId meal ID
     * @param whatsappNumber user's WhatsApp number
     */
    void deleteMeal(Long mealId, String whatsappNumber);
}