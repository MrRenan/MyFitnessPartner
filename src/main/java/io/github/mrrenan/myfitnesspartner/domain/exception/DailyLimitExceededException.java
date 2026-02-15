package io.github.mrrenan.myfitnesspartner.domain.exception;

/**
 * Exception thrown when daily meal limit is exceeded.
 */
public class DailyLimitExceededException extends DomainException {

    public DailyLimitExceededException(int maxMeals) {
        super("Daily meal limit exceeded. Maximum allowed: " + maxMeals + " meals per day");
    }
}