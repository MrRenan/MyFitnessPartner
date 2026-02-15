package io.github.mrrenan.myfitnesspartner.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Conversation entity storing chat history with AI.
 * Uses PostgreSQL JSONB to store messages efficiently.
 */
@Entity
@Table(name = "conversations", indexes = {
        @Index(name = "idx_user_conversation", columnList = "user_id,created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "whatsapp_message_id", length = 100)
    private String whatsappMessageId;

    /**
     * Messages stored as JSONB in PostgreSQL
     * Each message contains: role (user/assistant), content, timestamp
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "messages", columnDefinition = "jsonb")
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    @Column(name = "context", columnDefinition = "TEXT")
    private String context; // Additional context about the conversation

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Add a user message to the conversation
     */
    public void addUserMessage(String content) {
        Message message = Message.builder()
                .role("user")
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
        this.messages.add(message);
    }

    /**
     * Add an assistant message to the conversation
     */
    public void addAssistantMessage(String content) {
        Message message = Message.builder()
                .role("assistant")
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
        this.messages.add(message);
    }

    /**
     * Get the last N messages from conversation
     */
    public List<Message> getLastMessages(int count) {
        int size = messages.size();
        int fromIndex = Math.max(0, size - count);
        return new ArrayList<>(messages.subList(fromIndex, size));
    }

    /**
     * Inner class representing a single message in the conversation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role; // "user" or "assistant"
        private String content;
        private LocalDateTime timestamp;
    }
}