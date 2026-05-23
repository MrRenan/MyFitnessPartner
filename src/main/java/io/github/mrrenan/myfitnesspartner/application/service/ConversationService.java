package io.github.mrrenan.myfitnesspartner.application.service;

import io.github.mrrenan.myfitnesspartner.presentation.dto.ChatRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.ChatResponse;
import io.github.mrrenan.myfitnesspartner.domain.model.Conversation;

import java.util.List;

/**
 * Service interface for conversation management.
 * Orchestrates the chat flow between user and AI.
 */
public interface ConversationService {

    /**
     * Process a user message and return AI response.
     * Automatically saves the full conversation history.
     */
    ChatResponse chat(ChatRequest request);

    /**
     * Get all conversations for a user
     */
    List<Conversation> getHistory(String whatsappNumber);

    /**
     * Get the most recent conversation for a user
     */
    Conversation getLastConversation(String whatsappNumber);

}
