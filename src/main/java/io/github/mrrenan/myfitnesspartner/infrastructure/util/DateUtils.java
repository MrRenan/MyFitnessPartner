package io.github.mrrenan.myfitnesspartner.infrastructure.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Utility class for date and time operations.
 */
public class DateUtils {

    private DateUtils() {
        // Utility class
    }

    /**
     * Get the start of a specific day (00:00:00)
     */
    public static LocalDateTime getStartOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * Get the end of a specific day (23:59:59.999999999)
     */
    public static LocalDateTime getEndOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }

    /**
     * Get the start of today (00:00:00)
     */
    public static LocalDateTime getStartOfToday() {
        return getStartOfDay(LocalDate.now());
    }

    /**
     * Get the end of today (23:59:59.999999999)
     */
    public static LocalDateTime getEndOfToday() {
        return getEndOfDay(LocalDate.now());
    }

    /**
     * Get tomorrow as LocalDateTime
     */
    public static LocalDateTime getTomorrow() {
        return getStartOfDay(LocalDate.now().plusDays(1));
    }

    /**
     * Check if a LocalDateTime is today
     */
    public static boolean isToday(LocalDateTime dateTime) {
        LocalDate date = dateTime.toLocalDate();
        return date.equals(LocalDate.now());
    }

    /**
     * Get date range for last N days (including today)
     */
    public static LocalDateTime[] getLastNDaysRange(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        return new LocalDateTime[]{
                getStartOfDay(startDate),
                getEndOfDay(endDate)
        };
    }
}