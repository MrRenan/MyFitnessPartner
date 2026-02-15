package io.github.mrrenan.myfitnesspartner.presentation.dto;

import io.github.mrrenan.myfitnesspartner.domain.model.ActivityLevel;
import io.github.mrrenan.myfitnesspartner.domain.model.GoalType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating user information.
 * All fields are optional - only update what's provided.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @DecimalMin(value = "30.0", message = "Weight must be at least 30kg")
    @DecimalMax(value = "300.0", message = "Weight cannot exceed 300kg")
    private Double weight; // in kg

    @DecimalMin(value = "100.0", message = "Height must be at least 100cm")
    @DecimalMax(value = "250.0", message = "Height cannot exceed 250cm")
    private Double height; // in cm

    private ActivityLevel activityLevel;

    private GoalType goalType;
}