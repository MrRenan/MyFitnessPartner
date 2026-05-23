package io.github.mrrenan.myfitnesspartner.application.port.out;

import io.github.mrrenan.myfitnesspartner.application.dto.CalorieEstimate;

public interface FitnessAiPort {
    String chat(String userMessage, String conversationHistory);
    CalorieEstimate analyzeFood(String description);
}
