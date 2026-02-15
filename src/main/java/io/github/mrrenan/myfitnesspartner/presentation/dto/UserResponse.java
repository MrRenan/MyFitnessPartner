package io.github.mrrenan.myfitnesspartner.presentation.dto;

import io.github.mrrenan.myfitnesspartner.domain.model.ActivityLevel;
import io.github.mrrenan.myfitnesspartner.domain.model.Gender;
import io.github.mrrenan.myfitnesspartner.domain.model.GoalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for user response.
 * This is what we return to the API/WhatsApp when asking for user data.
 *
 * Notice: we don't expose database IDs, internal fields, or sensitive data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String whatsappNumber;
    private LocalDate dateOfBirth;
    private Integer age; // Calculated field
    private Gender gender;
    private Double weight;
    private Double height;
    private ActivityLevel activityLevel;
    private GoalType goalType;
    private Integer dailyCalorieGoal;
    private Double bmr; // Basal Metabolic Rate
    private Double tdee; // Total Daily Energy Expenditure
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}