package io.github.mrrenan.myfitnesspartner.application.service;

import io.github.mrrenan.myfitnesspartner.presentation.dto.DailyGoalResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for daily goal management.
 * Handles daily calorie tracking and progress monitoring.
 */
public interface DailyGoalService {

    /**
     * Get or create today's goal for a user
     * If goal doesn't exist for today, creates one automatically
     *
     * @param whatsappNumber user's WhatsApp number
     * @return today's daily goal
     */
    DailyGoalResponse getTodaysGoal(String whatsappNumber);

    /**
     * Get goal for a specific date
     * Creates one if it doesn't exist
     *
     * @param whatsappNumber user's WhatsApp number
     * @param date specific date
     * @return daily goal for the date
     */
    DailyGoalResponse getGoalByDate(String whatsappNumber, LocalDate date);

    /**
     * Get last N days of goals
     *
     * @param whatsappNumber user's WhatsApp number
     * @param days number of days (default 7)
     * @return list of daily goals
     */
    List<DailyGoalResponse> getLastDaysGoals(String whatsappNumber, int days);

    /**
     * Add calories to today's goal
     * Called internally when a meal is registered
     *
     * @param whatsappNumber user's WhatsApp number
     * @param calories calories to add
     * @return updated daily goal
     */
    DailyGoalResponse addCaloriesToToday(String whatsappNumber, int calories);

    /**
     * Reset today's goal (set calories consumed back to 0)
     * Useful if user wants to restart the day
     *
     * @param whatsappNumber user's WhatsApp number
     * @return reset daily goal
     */
    DailyGoalResponse resetTodaysGoal(String whatsappNumber);
}