package io.github.mrrenan.myfitnesspartner.presentation.mapper;

import io.github.mrrenan.myfitnesspartner.domain.model.Meal;
import io.github.mrrenan.myfitnesspartner.domain.model.User;
import io.github.mrrenan.myfitnesspartner.presentation.dto.CreateMealRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.MealResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper to convert between Meal entity and DTOs.
 */
@Component
public class MealMapper {

    /**
     * Convert CreateMealRequest DTO to Meal entity
     */
    public Meal toEntity(CreateMealRequest request, User user) {
        return Meal.builder()
                .user(user)
                .description(request.getDescription())
                .mealType(request.getMealType())
                .calories(request.getCalories())
                .protein(request.getProtein())
                .carbohydrates(request.getCarbohydrates())
                .fat(request.getFat())
                .notes(request.getNotes())
                .mealDate(request.getMealDate() != null ? request.getMealDate() : LocalDateTime.now())
                .build();
    }

    /**
     * Convert Meal entity to MealResponse DTO
     */
    public MealResponse toResponse(Meal meal) {
        return MealResponse.builder()
                .id(meal.getId())
                .userId(meal.getUser().getId())
                .description(meal.getDescription())
                .mealType(meal.getMealType())
                .calories(meal.getCalories())
                .protein(meal.getProtein())
                .carbohydrates(meal.getCarbohydrates())
                .fat(meal.getFat())
                .notes(meal.getNotes())
                .mealDate(meal.getMealDate())
                .imageUrl(meal.getImageUrl())
                .createdAt(meal.getCreatedAt())
                .updatedAt(meal.getUpdatedAt())
                .build();
    }
}