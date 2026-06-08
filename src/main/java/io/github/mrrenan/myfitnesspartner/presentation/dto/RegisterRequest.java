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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "WhatsApp number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Invalid WhatsApp number format")
    private String whatsappNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "30.0")
    @DecimalMax(value = "300.0")
    private Double weight;

    @NotNull(message = "Height is required")
    @DecimalMin(value = "100.0")
    @DecimalMax(value = "250.0")
    private Double height;

    @NotNull(message = "Activity level is required")
    private ActivityLevel activityLevel;

    @NotNull(message = "Goal type is required")
    private GoalType goalType;
}