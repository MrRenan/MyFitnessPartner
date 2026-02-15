package io.github.mrrenan.myfitnesspartner.domain.model;

/**
 * Enum representing the type of meal during the day.
 */
public enum MealType {
    BREAKFAST("Café da Manhã"),
    MORNING_SNACK("Lanche da Manhã"),
    LUNCH("Almoço"),
    AFTERNOON_SNACK("Lanche da Tarde"),
    DINNER("Jantar"),
    EVENING_SNACK("Ceia"),
    OTHER("Outro");

    private final String description;

    MealType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}