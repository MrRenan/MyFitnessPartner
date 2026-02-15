package io.github.mrrenan.myfitnesspartner.domain.repository;

import io.github.mrrenan.myfitnesspartner.domain.model.DailyGoal;
import io.github.mrrenan.myfitnesspartner.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DailyGoal entity operations.
 */
@Repository
public interface DailyGoalRepository extends JpaRepository<DailyGoal, Long> {

    /**
     * Find daily goal for a user on a specific date
     */
    Optional<DailyGoal> findByUserAndDate(User user, LocalDate date);

    /**
     * Find daily goals for a user within a date range
     */
    List<DailyGoal> findByUserAndDateBetweenOrderByDateDesc(
            User user,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * Find last N daily goals for a user
     */
    List<DailyGoal> findTop7ByUserOrderByDateDesc(User user);

    /**
     * Check if daily goal exists for user and date
     */
    boolean existsByUserAndDate(User user, LocalDate date);
}