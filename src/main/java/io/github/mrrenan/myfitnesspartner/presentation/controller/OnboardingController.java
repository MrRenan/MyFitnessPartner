package io.github.mrrenan.myfitnesspartner.presentation.controller;

import io.github.mrrenan.myfitnesspartner.application.service.onboarding.OnboardingResponse;
import io.github.mrrenan.myfitnesspartner.application.service.onboarding.OnboardingService;
import io.github.mrrenan.myfitnesspartner.presentation.dto.OnboardingChatRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/onboarding")
@RequiredArgsConstructor
@Tag(name = "Onboarding", description = "Conversational onboarding flow for new users")
public class OnboardingController {

    private final OnboardingService onboardingService;

    /**
     * Endpoint principal do onboarding conversacional.
     *
     * O frontend envia número + senha + mensagem a cada turno.
     * A resposta indica o status:
     * - ONBOARDING: IA ainda coletando dados, aiMessage contém a próxima pergunta
     * - COMPLETED: usuário criado, token JWT retornado para autenticar
     * - ERROR: algo deu errado, aiMessage explica o problema
     */
    @PostMapping("/chat")
    @Operation(
            summary = "Onboarding chat",
            description = "Send a message during onboarding. Returns ONBOARDING while collecting data, COMPLETED when user is created."
    )
    public ResponseEntity<OnboardingResponse> chat(@Valid @RequestBody OnboardingChatRequest request) {
        // Loga apenas prefixo do número para não expor PII em logs de produção
        log.info("POST /onboarding/chat - phone: {}***",
                request.getPhoneNumber().substring(0, Math.min(6, request.getPhoneNumber().length())));
        OnboardingResponse response = onboardingService.chat(
                request.getPhoneNumber(),
                request.getPassword(),
                request.getMessage()
        );
        return ResponseEntity.ok(response);
    }
}
