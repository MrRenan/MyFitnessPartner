package io.github.mrrenan.myfitnesspartner.domain.exception;

/**
 * Base exception for domain-related errors.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}