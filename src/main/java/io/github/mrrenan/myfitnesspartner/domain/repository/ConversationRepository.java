package io.github.mrrenan.myfitnesspartner.domain.repository;

import io.github.mrrenan.myfitnesspartner.domain.model.Conversation;
import io.github.mrrenan.myfitnesspartner.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Conversation entity operations.
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Find conversation by WhatsApp message ID
     */
    Optional<Conversation> findByWhatsappMessageId(String whatsappMessageId);

    /**
     * Find all conversations for a user, ordered by most recent
     */
    List<Conversation> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find conversations for a user within a date range
     */
    List<Conversation> findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(
            User user,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    /**
     * Find the most recent conversation for a user
     */
    Optional<Conversation> findFirstByUserOrderByCreatedAtDesc(User user);

    /**
     * Count total conversations for a user
     */
    long countByUser(User user);

    /**
     * Find conversations created today
     */
    @Query("SELECT c FROM Conversation c WHERE c.user = :user " +
            "AND c.createdAt >= :startOfDay AND c.createdAt < :endOfDay " +
            "ORDER BY c.createdAt DESC")
    List<Conversation> findTodaysConversationsByUser(
            @Param("user") User user,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}