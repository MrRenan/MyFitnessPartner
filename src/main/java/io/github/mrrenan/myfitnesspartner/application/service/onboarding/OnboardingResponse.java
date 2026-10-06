package io.github.mrrenan.myfitnesspartner.application.service.onboarding;

import lombok.Builder;
import lombok.Data;

/**
 * Resposta do endpoint de onboarding.
 * - Durante o onboarding: status=ONBOARDING, aiMessage com a próxima pergunta
 * - Quando completo: status=COMPLETED, token JWT para o frontend autenticar
 */
@Data
@Builder
public class OnboardingResponse {

    public enum Status {
        ONBOARDING,   // ainda coletando dados
        COMPLETED,    // usuário criado, pronto para usar
        ERROR         // algo deu errado
    }

    private Status status;
    private String aiMessage;

    // Preenchido apenas quando status=COMPLETED
    private String token;
    private String whatsappNumber;
    private String name;
}
