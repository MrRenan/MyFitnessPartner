package io.github.mrrenan.myfitnesspartner.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private Long conversationId;
    private String userMessage;
    private String aiResponse;
    private LocalDateTime timestamp;
}