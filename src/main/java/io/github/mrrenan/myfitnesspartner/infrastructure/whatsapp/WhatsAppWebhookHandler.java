package io.github.mrrenan.myfitnesspartner.infrastructure.whatsapp;

import io.github.mrrenan.myfitnesspartner.application.service.ConversationService;
import io.github.mrrenan.myfitnesspartner.domain.repository.UserRepository;
import io.github.mrrenan.myfitnesspartner.infrastructure.whatsapp.dto.WhatsAppWebhookPayload;
import io.github.mrrenan.myfitnesspartner.presentation.dto.ChatRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Processa os eventos recebidos pelo webhook do WhatsApp.
 * Executa de forma assíncrona para retornar 200 imediatamente para a Meta.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WhatsAppWebhookHandler {

    private final ConversationService conversationService;
    private final WhatsAppMessageSender messageSender;
    private final UserRepository userRepository;

    /**
     * Processa o payload recebido de forma assíncrona.
     * O @Async garante que o controller retorna 200 imediatamente.
     */
    @Async("taskExecutor")
    public void handle(WhatsAppWebhookPayload payload) {
        try {
            if (payload.getEntry() == null) return;

            payload.getEntry().forEach(entry -> {
                if (entry.getChanges() == null) return;

                entry.getChanges().forEach(change -> {
                    WhatsAppWebhookPayload.Value value = change.getValue();
                    if (value == null || value.getMessages() == null) return;

                    value.getMessages().forEach(message ->
                            processMessage(message, value.getMetadata()));
                });
            });

        } catch (Exception e) {
            log.error("Erro ao processar payload do webhook: {}", e.getMessage(), e);
        }
    }

    /**
     * Processa uma mensagem individual
     */
    private void processMessage(
            WhatsAppWebhookPayload.Message message,
            WhatsAppWebhookPayload.Metadata metadata) {

        // Só processa mensagens de texto por enquanto
        if (!"text".equals(message.getType())) {
            log.debug("Tipo de mensagem não suportado: {}", message.getType());
            return;
        }

        String from = "+" + message.getFrom(); // ex: +5511999999999
        String text = message.getText().getBody();

        log.info("Mensagem recebida de {}: {}", from, text);

        // Verifica se o usuário está cadastrado
        boolean userExists = userRepository
                .findByWhatsappNumberAndIsActiveTrue(from)
                .isPresent();

        if (!userExists) {
            log.warn("Usuário não cadastrado: {}", from);
            messageSender.sendMessage(from,
                    "Olá! 👋 Você ainda não está cadastrado no MyFitnessPartner.\n\n" +
                            "Para começar, cadastre-se pelo nosso app e depois volte aqui!");
            return;
        }

        // Envia indicador de processamento
        messageSender.sendMessage(from, "⏳ Processando...");

        // Chama a IA via ConversationService
        ChatRequest chatRequest = ChatRequest.builder()
                .whatsappNumber(from)
                .message(text)
                .build();

        ChatResponse response = conversationService.chat(chatRequest);

        // Envia a resposta
        messageSender.sendMessage(from, response.getAiResponse());

        log.info("Resposta enviada para: {}", from);
    }
}