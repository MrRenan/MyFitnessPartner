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

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User entity representing a fitness partner user.
 * Stores user profile, physical data, and fitness goals.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_whatsapp_number", columnList = "whatsapp_number", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "WhatsApp number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Invalid WhatsApp number format")
    @Column(name = "whatsapp_number", nullable = false, unique = true, length = 20)
    private String whatsappNumber;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "30.0", message = "Weight must be at least 30kg")
    @DecimalMax(value = "300.0", message = "Weight must not exceed 300kg")
    @Column(nullable = false)
    private Double weight; // in kg

    @NotNull(message = "Height is required")
    @DecimalMin(value = "100.0", message = "Height must be at least 100cm")
    @DecimalMax(value = "250.0", message = "Height must not exceed 250cm")
    @Column(nullable = false)
    private Double height; // in cm

    @NotNull(message = "Gender is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @NotNull(message = "Activity level is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false, length = 30)
    private ActivityLevel activityLevel;

    @NotNull(message = "Goal type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 30)
    private GoalType goalType;

    @DecimalMin(value = "1000", message = "Daily calorie goal must be at least 1000")
    @DecimalMax(value = "5000", message = "Daily calorie goal must not exceed 5000")
    @Column(name = "daily_calorie_goal")
    private Integer dailyCalorieGoal; // calculated based on profile

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Calculate Basal Metabolic Rate (BMR) using Mifflin-St Jeor Equation
     * BMR = 10 * weight(kg) + 6.25 * height(cm) - 5 * age + s
     * where s is +5 for males and -161 for females
     */
    public double calculateBMR() {
        int age = LocalDate.now().getYear() - dateOfBirth.getYear();
        return 10 * weight + 6.25 * height - 5 * age + gender.getBmrModifier();
    }

    /**
     * Calculate Total Daily Energy Expenditure (TDEE)
     * TDEE = BMR * Activity Level Multiplier
     */
    public double calculateTDEE() {
        return calculateBMR() * activityLevel.getMultiplier();
    }

    /**
     * Calculate daily calorie goal based on TDEE and goal type
     */
    public int calculateDailyCalorieGoal() {
        return (int) Math.round(calculateTDEE() + goalType.getCalorieAdjustment());
    }

    /**
     * Update daily calorie goal based on current profile
     */
    public void updateCalorieGoal() {
        this.dailyCalorieGoal = calculateDailyCalorieGoal();
    }
}