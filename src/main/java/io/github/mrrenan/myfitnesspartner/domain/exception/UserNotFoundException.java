package io.github.mrrenan.myfitnesspartner.domain.exception;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends DomainException {

    public UserNotFoundException(String whatsappNumber) {
        super("User not found with WhatsApp number: " + whatsappNumber);
    }

    public UserNotFoundException(Long userId) {
        super("User not found with ID: " + userId);
    }
}