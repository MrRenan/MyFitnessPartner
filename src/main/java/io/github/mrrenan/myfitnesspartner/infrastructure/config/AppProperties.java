package io.github.mrrenan.myfitnesspartner.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Application-specific configuration properties.
 * Maps properties from application.yml with prefix 'app'.
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @NotNull
    private Fitness fitness = new Fitness();

    @NotNull
    private Ai ai = new Ai();

    @Data
    public static class Fitness {
        @Min(value = 1000, message = "Default calorie goal must be at least 1000")
        private Integer defaultCalorieGoal;

        @Min(value = 1, message = "Max daily meals must be at least 1")
        private Integer maxDailyMeals;
    }

    @Data
    public static class Ai {
        @NotBlank(message = "System prompt is required")
        private String systemPrompt;
    }
}