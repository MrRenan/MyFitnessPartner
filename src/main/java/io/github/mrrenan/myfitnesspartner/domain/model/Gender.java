package io.github.mrrenan.myfitnesspartner.domain.model;

/**
 * Enum representing biological gender for BMR calculation.
 */
public enum Gender {
    MALE("Masculino", 5),
    FEMALE("Feminino", -161);

    private final String description;
    private final int bmrModifier;

    Gender(String description, int bmrModifier) {
        this.description = description;
        this.bmrModifier = bmrModifier;
    }

    public String getDescription() {
        return description;
    }

    /**
     * BMR modifier for Mifflin-St Jeor equation
     * Male: +5, Female: -161
     */
    public int getBmrModifier() {
        return bmrModifier;
    }
}