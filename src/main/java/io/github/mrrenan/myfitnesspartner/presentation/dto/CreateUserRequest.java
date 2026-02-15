package io.github.mrrenan.myfitnesspartner.presentation.dto;

import io.github.mrrenan.myfitnesspartner.domain.model.ActivityLevel;
import io.github.mrrenan.myfitnesspartner.domain.model.Gender;
import io.github.mrrenan.myfitnesspartner.domain.model.GoalType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for creating a new user.
 * This is what we receive from the API/WhatsApp when registering a user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "WhatsApp number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "WhatsApp number must be in format +5511999999999")
    private String whatsappNumber;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "30.0", message = "Weight must be at least 30kg")
    @DecimalMax(value = "300.0", message = "Weight cannot exceed 300kg")
    private Double weight; // in kg

    @NotNull(message = "Height is required")
    @DecimalMin(value = "100.0", message = "Height must be at least 100cm")
    @DecimalMax(value = "250.0", message = "Height cannot exceed 250cm")
    private Double height; // in cm

    @NotNull(message = "Activity level is required")
    private ActivityLevel activityLevel;

    @NotNull(message = "Goal type is required")
    private GoalType goalType;
}