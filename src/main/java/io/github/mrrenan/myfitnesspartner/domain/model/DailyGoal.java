package io.github.mrrenan.myfitnesspartner.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DailyGoal entity representing a user's daily calorie tracking.
 * One record per user per day.
 */
@Entity
@Table(name = "daily_goals",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"}),
        indexes = {
                @Index(name = "idx_user_date", columnList = "user_id,date"),
                @Index(name = "idx_date", columnList = "date")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "Date is required")
    @Column(nullable = false)
    private LocalDate date;

    @NotNull(message = "Calorie goal is required")
    @Min(value = 1000, message = "Calorie goal must be at least 1000")
    @Column(name = "calorie_goal", nullable = false)
    private Integer calorieGoal;

    @Builder.Default
    @Min(value = 0, message = "Consumed calories cannot be negative")
    @Column(name = "calories_consumed", nullable = false)
    private Integer caloriesConsumed = 0;

    @Builder.Default
    @Min(value = 0, message = "Meal count cannot be negative")
    @Column(name = "meal_count", nullable = false)
    private Integer mealCount = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Calculate remaining calories for the day
     */
    public int getRemainingCalories() {
        return calorieGoal - caloriesConsumed;
    }

    /**
     * Calculate progress percentage
     */
    public double getProgressPercentage() {
        if (calorieGoal == 0) return 0;
        return (double) caloriesConsumed / calorieGoal * 100;
    }

    /**
     * Check if daily goal was met (within 10% tolerance)
     */
    public boolean isGoalMet() {
        double percentage = getProgressPercentage();
        return percentage >= 90 && percentage <= 110;
    }

    /**
     * Add meal calories to daily total
     */
    public void addMeal(int calories) {
        this.caloriesConsumed += calories;
        this.mealCount++;
    }
}