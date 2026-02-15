package io.github.mrrenan.myfitnesspartner.domain.model;

/**
 * Enum representing the user's fitness goal.
 */
public enum GoalType {
    LOSE_WEIGHT("Perder Peso", -500),
    MAINTAIN_WEIGHT("Manter Peso", 0),
    GAIN_WEIGHT("Ganhar Peso", 500);

    private final String description;
    private final int calorieAdjustment;

    GoalType(String description, int calorieAdjustment) {
        this.description = description;
        this.calorieAdjustment = calorieAdjustment;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Calorie adjustment to apply to maintenance calories
     * Negative for weight loss, positive for weight gain
     */
    public int getCalorieAdjustment() {
        return calorieAdjustment;
    }
}