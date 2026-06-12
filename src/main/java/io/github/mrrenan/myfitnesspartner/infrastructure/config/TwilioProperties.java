package io.github.mrrenan.myfitnesspartner.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "twilio")
public class TwilioProperties {

    @NotBlank(message = "Twilio Account SID is required")
    private String accountSid;

    @NotBlank(message = "Twilio Auth Token is required")
    private String authToken;

    /**
     * Número do sandbox do Twilio
     * Formato: whatsapp:+14155238886
     */
    @NotBlank(message = "Twilio WhatsApp number is required")
    private String whatsappNumber;
}