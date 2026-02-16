package io.github.mrrenan.myfitnesspartner.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing calorie and nutritional estimates from AI.
 * Returned by GeminiService after analyzing a meal description.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalorieEstimate {

    private Integer calories;
    private Double protein;      // in grams
    private Double carbohydrates; // in grams
    private Double fat;          // in grams
    private String explanation;  // AI's reasoning about the calculation
    private Double confidence;   // 0.0 to 1.0 confidence level
}