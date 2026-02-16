package io.github.mrrenan.myfitnesspartner.presentation.mapper;

import io.github.mrrenan.myfitnesspartner.domain.model.DailyGoal;
import io.github.mrrenan.myfitnesspartner.presentation.dto.DailyGoalResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert DailyGoal entity to DTO.
 */
@Component
public class DailyGoalMapper {

    /**
     * Convert DailyGoal entity to DailyGoalResponse DTO
     */
    public DailyGoalResponse toResponse(DailyGoal dailyGoal) {
        return DailyGoalResponse.builder()
                .id(dailyGoal.getId())
                .userId(dailyGoal.getUser().getId())
                .date(dailyGoal.getDate())
                .calorieGoal(dailyGoal.getCalorieGoal())
                .caloriesConsumed(dailyGoal.getCaloriesConsumed())
                .remainingCalories(dailyGoal.getRemainingCalories())
                .mealCount(dailyGoal.getMealCount())
                .progressPercentage(dailyGoal.getProgressPercentage())
                .goalMet(dailyGoal.isGoalMet())
                .status(determineStatus(dailyGoal.getProgressPercentage()))
                .createdAt(dailyGoal.getCreatedAt())
                .updatedAt(dailyGoal.getUpdatedAt())
                .build();
    }

    /**
     * Determine status based on progress percentage
     */
    private String determineStatus(double progressPercentage) {
        if (progressPercentage < 80) {
            return "Under Goal";
        } else if (progressPercentage <= 110) {
            return "On Track";
        } else {
            return "Over Goal";
        }
    }
}