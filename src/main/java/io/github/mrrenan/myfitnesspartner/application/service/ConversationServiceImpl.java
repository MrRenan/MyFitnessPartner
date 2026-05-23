package io.github.mrrenan.myfitnesspartner.application.service;

import io.github.mrrenan.myfitnesspartner.application.port.out.FitnessAiPort;
import io.github.mrrenan.myfitnesspartner.domain.exception.UserNotFoundException;
import io.github.mrrenan.myfitnesspartner.domain.model.Conversation;
import io.github.mrrenan.myfitnesspartner.domain.model.User;
import io.github.mrrenan.myfitnesspartner.domain.repository.ConversationRepository;
import io.github.mrrenan.myfitnesspartner.domain.repository.UserRepository;
import io.github.mrrenan.myfitnesspartner.presentation.dto.ChatRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService{

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final FitnessAiPort fitnessAiPort;

    // Quantas mensagens anteriores enviar como contexto para a IA
    private static final int CONTEXT_MESSAGE_COUNT = 6;

    @Override
    @Transactional
    public ChatResponse chat(ChatRequest request) {
        // 1. Busca o usuário
        User user = findUserByWhatsapp(request.getWhatsappNumber());

        // 2. Busca a conversa mais recente ou cria uma nova
        Conversation conversation = conversationRepository
                .findFirstByUserOrderByCreatedAtDesc(user)
                .orElseGet(() -> {
                    log.info("Creating new conversation for user: {}", request.getWhatsappNumber());
                    return Conversation.builder()
                            .user(user)
                            .build();
                });

        // 3. Adiciona mensagem do usuário
        conversation.addUserMessage(request.getMessage());

        // 4. Monta contexto com últimas mensagens
        String context = buildContext(conversation);

        // 5. Chama a IA
        log.debug("Calling AI with context of {} messages", CONTEXT_MESSAGE_COUNT);
        String aiResponse = fitnessAiPort.chat(request.getMessage(), context);

        // 6. Adiciona resposta da IA na conversa
        conversation.addAssistantMessage(aiResponse);

        // 7. Salva
        Conversation saved = conversationRepository.save(conversation);
        log.info("Conversation saved with ID: {}", saved.getId());

        // 8. Retorna resposta
        return ChatResponse.builder()
                .conversationId(saved.getId())
                .userMessage(request.getMessage())
                .aiResponse(aiResponse)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Conversation> getHistory(String whatsappNumber) {
        log.debug("Getting conversation history for user: {}", whatsappNumber);
        User user = findUserByWhatsapp(whatsappNumber);
        return conversationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Conversation getLastConversation(String whatsappNumber) {
        log.debug("Getting last conversation for user: {}", whatsappNumber);
        User user = findUserByWhatsapp(whatsappNumber);
        return conversationRepository.findFirstByUserOrderByCreatedAtDesc(user)
                .orElse(null);
    }

    /**
     * Monta o contexto das últimas N mensagens para enviar para a IA
     */
    private String buildContext(Conversation conversation) {
        List<Conversation.Message> lastMessages = conversation.getLastMessages(CONTEXT_MESSAGE_COUNT);

        if (lastMessages.isEmpty()) {
            return "";
        }

        return lastMessages.stream()
                .map(m -> m.getRole().toUpperCase() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));
    }

    private User findUserByWhatsapp(String whatsappNumber) {
        return userRepository.findByWhatsappNumberAndIsActiveTrue(whatsappNumber)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", whatsappNumber);
                    return new UserNotFoundException(whatsappNumber);
                });
    }
}
