package io.github.mrrenan.myfitnesspartner.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Meal entity representing a registered meal with nutritional information.
 */
@Entity
@Table(name = "meals", indexes = {
        @Index(name = "idx_user_meal_date", columnList = "user_id,meal_date"),
        @Index(name = "idx_meal_date", columnList = "meal_date")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Description is required")
    @Size(min = 3, max = 500, message = "Description must be between 3 and 500 characters")
    @Column(nullable = false, length = 500)
    private String description;

    @NotNull(message = "Meal type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 30)
    private MealType mealType;

    @NotNull(message = "Calories is required")
    @Min(value = 1, message = "Calories must be at least 1")
    @Max(value = 5000, message = "Calories must not exceed 5000")
    @Column(nullable = false)
    private Integer calories;

    @Min(value = 0, message = "Protein cannot be negative")
    @Column
    private Double protein; // in grams

    @Min(value = 0, message = "Carbohydrates cannot be negative")
    @Column
    private Double carbohydrates; // in grams

    @Min(value = 0, message = "Fat cannot be negative")
    @Column
    private Double fat; // in grams

    @Column(name = "meal_date", nullable = false)
    private LocalDateTime mealDate;

    @Column(length = 1000)
    private String notes;

    @Column(name = "image_url", length = 500)
    private String imageUrl; // URL of meal photo if uploaded

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (mealDate == null) {
            mealDate = LocalDateTime.now();
        }
    }
}