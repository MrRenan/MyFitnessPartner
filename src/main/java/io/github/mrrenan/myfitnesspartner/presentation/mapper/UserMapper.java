package io.github.mrrenan.myfitnesspartner.presentation.mapper;

import io.github.mrrenan.myfitnesspartner.domain.model.User;
import io.github.mrrenan.myfitnesspartner.presentation.dto.CreateUserRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.UpdateUserRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.UserResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

/**
 * Mapper to convert between User entity and DTOs.
 * Centralizes conversion logic to avoid duplication.
 */
@Component
public class UserMapper {

    /**
     * Convert CreateUserRequest DTO to User entity
     */
    public User toEntity(CreateUserRequest request) {
        User user = User.builder()
                .name(request.getName())
                .whatsappNumber(request.getWhatsappNumber())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .weight(request.getWeight())
                .height(request.getHeight())
                .activityLevel(request.getActivityLevel())
                .goalType(request.getGoalType())
                .isActive(true)
                .build();

        // Calculate and set the daily calorie goal
        user.updateCalorieGoal();

        return user;
    }

    /**
     * Convert User entity to UserResponse DTO
     */
    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .whatsappNumber(user.getWhatsappNumber())
                .dateOfBirth(user.getDateOfBirth())
                .age(calculateAge(user.getDateOfBirth()))
                .gender(user.getGender())
                .weight(user.getWeight())
                .height(user.getHeight())
                .activityLevel(user.getActivityLevel())
                .goalType(user.getGoalType())
                .dailyCalorieGoal(user.getDailyCalorieGoal())
                .bmr(user.calculateBMR())
                .tdee(user.calculateTDEE())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Update User entity with data from UpdateUserRequest
     * Only updates fields that are not null in the request
     */
    public void updateEntity(User user, UpdateUserRequest request) {
        boolean needsRecalculation = false;

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getWeight() != null) {
            user.setWeight(request.getWeight());
            needsRecalculation = true;
        }

        if (request.getHeight() != null) {
            user.setHeight(request.getHeight());
            needsRecalculation = true;
        }

        if (request.getActivityLevel() != null) {
            user.setActivityLevel(request.getActivityLevel());
            needsRecalculation = true;
        }

        if (request.getGoalType() != null) {
            user.setGoalType(request.getGoalType());
            needsRecalculation = true;
        }

        // Recalculate calorie goal if any relevant field changed
        if (needsRecalculation) {
            user.updateCalorieGoal();
        }
    }

    /**
     * Calculate age from date of birth
     */
    private Integer calculateAge(LocalDate dateOfBirth) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}