package io.github.mrrenan.myfitnesspartner.presentation.controller;

import io.github.mrrenan.myfitnesspartner.application.service.ConversationService;
import io.github.mrrenan.myfitnesspartner.domain.model.Conversation;
import io.github.mrrenan.myfitnesspartner.presentation.dto.ChatRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.ChatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "Chat with AI fitness assistant")
public class ConversationController {

    private final ConversationService conversationService;

    @PostMapping("/chat")
    @Operation(
            summary = "Send message",
            description = "Send a message to the AI fitness assistant and get a response"
    )
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("POST /conversations/chat - User: {}", request.getWhatsappNumber());
        ChatResponse response = conversationService.chat(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @Operation(
            summary = "Get history",
            description = "Get all conversations for a user"
    )
    public ResponseEntity<List<Conversation>> getHistory(@RequestParam String whatsappNumber) {
        log.info("GET /conversations/history - User: {}", whatsappNumber);
        List<Conversation> history = conversationService.getHistory(whatsappNumber);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/last")
    @Operation(
            summary = "Get last conversation",
            description = "Get the most recent conversation for a user"
    )
    public ResponseEntity<Conversation> getLastConversation(@RequestParam String whatsappNumber) {
        log.info("GET /conversations/last - User: {}", whatsappNumber);
        Conversation conversation = conversationService.getLastConversation(whatsappNumber);
        return ResponseEntity.ok(conversation);
    }
}
