package io.github.mrrenan.myfitnesspartner.presentation.mapper;

import io.github.mrrenan.myfitnesspartner.domain.model.*;
import io.github.mrrenan.myfitnesspartner.presentation.dto.ConversationResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConversationMapper")
class ConversationMapperTest {

    private final ConversationMapper conversationMapper = new ConversationMapper();

    private User buildUser() {
        return User.builder()
                .id(1L)
                .name("Renan")
                .whatsappNumber("+5511999999999")
                .password("senha")
                .dateOfBirth(LocalDate.of(1994, 2, 18))
                .gender(Gender.MALE)
                .weight(93.0)
                .height(174.0)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .goalType(GoalType.LOSE_WEIGHT)
                .dailyCalorieGoal(2387)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("deve converter Conversation para ConversationResponse")
    void toResponse_shouldMapCorrectly() {
        // arrange
        User user = buildUser();
        Conversation conversation = Conversation.builder()
                .id(1L)
                .user(user)
                .build();
        conversation.addUserMessage("Qual minha meta?");
        conversation.addAssistantMessage("Sua meta é 2387 calorias!");

        // act
        ConversationResponse response = conversationMapper.toResponse(conversation);

        // assert
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUserName()).isEqualTo("Renan");
        assertThat(response.getMessages()).hasSize(2);
        assertThat(response.getMessages().get(0).getRole()).isEqualTo("user");
        assertThat(response.getMessages().get(1).getRole()).isEqualTo("assistant");
    }

    @Test
    @DisplayName("deve converter lista de Conversations")
    void toResponseList_shouldMapAllConversations() {
        // arrange
        User user = buildUser();
        List<Conversation> conversations = List.of(
                Conversation.builder().id(1L).user(user).build(),
                Conversation.builder().id(2L).user(user).build()
        );

        // act
        List<ConversationResponse> responses =
                conversationMapper.toResponseList(conversations);

        // assert
        assertThat(responses).hasSize(2);
    }
}