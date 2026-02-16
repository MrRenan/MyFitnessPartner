package io.github.mrrenan.myfitnesspartner.presentation.dto;

import io.github.mrrenan.myfitnesspartner.domain.model.MealType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating a new meal.
 * Used when user registers what they ate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMealRequest {

    @NotBlank(message = "WhatsApp number is required")
    private String whatsappNumber;

    @NotBlank(message = "Description is required")
    @Size(min = 3, max = 500, message = "Description must be between 3 and 500 characters")
    private String description;

    @NotNull(message = "Meal type is required")
    private MealType mealType;

    /**
     * Calories - Optional when using AI to calculate
     * Required when manually registering
     */
    @Min(value = 1, message = "Calories must be at least 1")
    @Max(value = 5000, message = "Calories must not exceed 5000")
    private Integer calories;

    @Min(value = 0, message = "Protein cannot be negative")
    private Double protein; // in grams (optional)

    @Min(value = 0, message = "Carbohydrates cannot be negative")
    private Double carbohydrates; // in grams (optional)

    @Min(value = 0, message = "Fat cannot be negative")
    private Double fat; // in grams (optional)

    private String notes; // Optional notes about the meal

    private LocalDateTime mealDate; // Optional, defaults to now
}