package io.github.mrrenan.myfitnesspartner.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "WhatsApp number is required")
    private String whatsappNumber;

    @NotBlank(message = "Message is required")
    private String message;
}
