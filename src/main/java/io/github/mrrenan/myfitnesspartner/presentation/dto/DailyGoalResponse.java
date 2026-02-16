package io.github.mrrenan.myfitnesspartner.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for daily goal response.
 * Shows the user's calorie tracking for a specific day.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyGoalResponse {

    private Long id;
    private Long userId;
    private LocalDate date;
    private Integer calorieGoal;
    private Integer caloriesConsumed;
    private Integer remainingCalories;
    private Integer mealCount;
    private Double progressPercentage;
    private Boolean goalMet;
    private String status; // "Under Goal", "On Track", "Over Goal"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}