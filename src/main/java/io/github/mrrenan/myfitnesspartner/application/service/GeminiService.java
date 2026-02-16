package io.github.mrrenan.myfitnesspartner.application.service;

import io.github.mrrenan.myfitnesspartner.application.dto.CalorieEstimate;

/**
 * Service interface for Gemini AI integration.
 * Handles natural language processing for meal analysis.
 */
public interface GeminiService {

    /**
     * Calculate calories and macros from meal description
     *
     * Example input: "200g de frango grelhado com arroz integral e salada"
     *
     * @param description natural language description of the meal
     * @return estimated calories and nutritional information
     */
    CalorieEstimate calculateCaloriesFromDescription(String description);

    /**
     * Generate a general fitness-related response
     * Used for answering questions about diet, workouts, etc.
     *
     * @param userMessage user's question or message
     * @param context optional context from previous conversation
     * @return AI-generated response
     */
    String generateFitnessResponse(String userMessage, String context);
}