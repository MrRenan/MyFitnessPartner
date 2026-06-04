package io.github.mrrenan.myfitnesspartner.presentation.mapper;

import io.github.mrrenan.myfitnesspartner.domain.model.Conversation;
import io.github.mrrenan.myfitnesspartner.presentation.dto.ConversationResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConversationMapper {

    public ConversationResponse toResponse(Conversation conversation) {
        return ConversationResponse.builder()
                .id(conversation.getId())
                .userId(conversation.getUser().getId())
                .userName(conversation.getUser().getName())
                .messages(mapMessages(conversation.getMessages()))
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    public List<ConversationResponse> toResponseList(List<Conversation> conversations) {
        return conversations.stream()
                .map(this::toResponse)
                .toList();
    }

    private List<ConversationResponse.MessageResponse> mapMessages(
            List<Conversation.Message> messages) {
        return messages.stream()
                .map(m -> ConversationResponse.MessageResponse.builder()
                        .role(m.getRole())
                        .content(m.getContent())
                        .timestamp(m.getTimestamp())
                        .build())
                .toList();
    }
}