package io.github.mrrenan.myfitnesspartner.domain.model;

/**
 * Enum representing the user's physical activity level.
 * Used to calculate daily calorie needs.
 */
public enum ActivityLevel {
    SEDENTARY("Sedentário", 1.2),
    LIGHTLY_ACTIVE("Levemente Ativo", 1.375),
    MODERATELY_ACTIVE("Moderadamente Ativo", 1.55),
    VERY_ACTIVE("Muito Ativo", 1.725),
    EXTREMELY_ACTIVE("Extremamente Ativo", 1.9);

    private final String description;
    private final double multiplier;

    ActivityLevel(String description, double multiplier) {
        this.description = description;
        this.multiplier = multiplier;
    }

    public String getDescription() {
        return description;
    }

    public double getMultiplier() {
        return multiplier;
    }
}