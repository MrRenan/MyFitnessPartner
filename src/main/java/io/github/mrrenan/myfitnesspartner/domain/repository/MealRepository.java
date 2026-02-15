package io.github.mrrenan.myfitnesspartner.domain.repository;

import io.github.mrrenan.myfitnesspartner.domain.model.Meal;
import io.github.mrrenan.myfitnesspartner.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Meal entity operations.
 */
@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {

    /**
     * Find all meals for a user
     */
    List<Meal> findByUserOrderByMealDateDesc(User user);

    /**
     * Find meals for a user within a date range
     */
    List<Meal> findByUserAndMealDateBetweenOrderByMealDateDesc(
            User user,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * Find today's meals for a user
     */
    @Query("SELECT m FROM Meal m WHERE m.user = :user " +
            "AND m.mealDate >= :startOfDay AND m.mealDate < :endOfDay " +
            "ORDER BY m.mealDate DESC")
    List<Meal> findTodaysMealsByUser(
            @Param("user") User user,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    /**
     * Calculate total calories for a user on a specific date
     */
    @Query("SELECT COALESCE(SUM(m.calories), 0) FROM Meal m " +
            "WHERE m.user = :user " +
            "AND m.mealDate >= :startOfDay AND m.mealDate < :endOfDay")
    Integer sumCaloriesByUserAndDate(
            @Param("user") User user,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    /**
     * Count meals for a user on a specific date
     */
    @Query("SELECT COUNT(m) FROM Meal m " +
            "WHERE m.user = :user " +
            "AND m.mealDate >= :startOfDay AND m.mealDate < :endOfDay")
    Long countMealsByUserAndDate(
            @Param("user") User user,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}