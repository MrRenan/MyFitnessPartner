package io.github.mrrenan.myfitnesspartner.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Configuration properties for WhatsApp Business Cloud API (Meta) integration.
 * Maps properties from application.yml with prefix 'whatsapp'.
 */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "whatsapp")
public class WhatsAppProperties {

    /**
     * WhatsApp Business API URL
     * Default: https://graph.facebook.com/v18.0
     */
    @NotBlank(message = "WhatsApp API URL is required")
    private String apiUrl;

    /**
     * Phone Number ID from WhatsApp Business API
     */
    @NotBlank(message = "Phone Number ID is required")
    private String phoneNumberId;

    /**
     * WhatsApp Business Account ID
     */
    @NotBlank(message = "Business Account ID is required")
    private String businessAccountId;

    /**
     * Access Token for WhatsApp Business API
     */
    @NotBlank(message = "Access Token is required")
    private String accessToken;

    /**
     * Verify Token for webhook validation
     * This is a custom token you define
     */
    @NotBlank(message = "Verify Token is required")
    private String verifyToken;

    /**
     * Webhook URL where Meta will send messages
     */
    @NotBlank(message = "Webhook URL is required")
    private String webhookUrl;
}