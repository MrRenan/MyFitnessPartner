package io.github.mrrenan.myfitnesspartner.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Configuration properties for Google Gemini AI integration.
 * Maps properties from application.yml with prefix 'gemini'.
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "gemini")
public class GeminiProperties {

    /**
     * Google Gemini API Key
     * Get it at: https://makersuite.google.com/app/apikey
     */
    @NotBlank(message = "Gemini API Key is required")
    private String apiKey;

    /**
     * Model to use (e.g., gemini-1.5-pro, gemini-1.5-flash)
     */
    @NotBlank(message = "Gemini model is required")
    private String model;

    /**
     * Maximum number of tokens to generate in the completion
     */
    @NotNull(message = "Max tokens is required")
    @Min(value = 1, message = "Max tokens must be at least 1")
    @Max(value = 8192, message = "Max tokens cannot exceed 8192")
    private Integer maxTokens;

    /**
     * Sampling temperature (0.0 to 2.0)
     * Higher values make output more random, lower values more deterministic
     */
    @NotNull(message = "Temperature is required")
    @Min(value = 0, message = "Temperature must be at least 0.0")
    @Max(value = 2, message = "Temperature cannot exceed 2.0")
    private Double temperature;
}