package io.github.mrrenan.myfitnesspartner.presentation.dto;

import io.github.mrrenan.myfitnesspartner.domain.model.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for meal response.
 * Returns meal information to the client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealResponse {

    private Long id;
    private Long userId;
    private String description;
    private MealType mealType;
    private Integer calories;
    private Double protein;
    private Double carbohydrates;
    private Double fat;
    private String notes;
    private LocalDateTime mealDate;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}