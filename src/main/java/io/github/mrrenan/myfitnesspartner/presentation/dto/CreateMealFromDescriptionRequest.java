package io.github.mrrenan.myfitnesspartner.presentation.dto;

import io.github.mrrenan.myfitnesspartner.domain.model.MealType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a meal from natural language description.
 * The AI will calculate calories, protein, carbs, and fat automatically.
 *
 * Example: "Comi 200g de frango grelhado com 150g de arroz integral e salada"
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMealFromDescriptionRequest {

    @NotBlank(message = "WhatsApp number is required")
    private String whatsappNumber;

    @NotBlank(message = "Description is required")
    @Size(min = 5, max = 500, message = "Description must be between 5 and 500 characters")
    private String description;

    @NotNull(message = "Meal type is required")
    private MealType mealType;

    private String notes; // Optional notes
}