package io.github.mrrenan.myfitnesspartner.domain.repository;

import io.github.mrrenan.myfitnesspartner.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by WhatsApp number
     */
    Optional<User> findByWhatsappNumber(String whatsappNumber);

    /**
     * Check if user exists by WhatsApp number
     */
    boolean existsByWhatsappNumber(String whatsappNumber);

    /**
     * Find active users only
     */
    Optional<User> findByWhatsappNumberAndIsActiveTrue(String whatsappNumber);
}