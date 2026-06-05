package io.github.mrrenan.myfitnesspartner.application.service;

import io.github.mrrenan.myfitnesspartner.application.port.out.FitnessAiPort;
import io.github.mrrenan.myfitnesspartner.domain.exception.UserNotFoundException;
import io.github.mrrenan.myfitnesspartner.domain.model.Conversation;
import io.github.mrrenan.myfitnesspartner.domain.model.User;
import io.github.mrrenan.myfitnesspartner.domain.repository.ConversationRepository;
import io.github.mrrenan.myfitnesspartner.domain.repository.UserRepository;
import io.github.mrrenan.myfitnesspartner.presentation.dto.ChatRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.ChatResponse;
import io.github.mrrenan.myfitnesspartner.presentation.dto.ConversationResponse;
import io.github.mrrenan.myfitnesspartner.presentation.mapper.ConversationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConversationServiceImpl")
class ConversationServiceImplTest {

    @Mock private ConversationRepository conversationRepository;
    @Mock private UserRepository userRepository;
    @Mock private FitnessAiPort fitnessAiPort;
    @Mock private ConversationMapper conversationMapper;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    private User user;
    private ChatRequest chatRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Renan")
                .whatsappNumber("+5511999999999")
                .isActive(true)
                .build();

        chatRequest = ChatRequest.builder()
                .whatsappNumber("+5511999999999")
                .message("Comi um pão com ovo, está dentro da meta?")
                .build();
    }

    @Test
    @DisplayName("deve criar nova conversa quando usuário não tem conversa anterior")
    void chat_shouldCreateNewConversation_whenNoExistingConversation() {
        // arrange
        when(userRepository.findByWhatsappNumberAndIsActiveTrue("+5511999999999"))
                .thenReturn(Optional.of(user));
        when(conversationRepository.findFirstByUserOrderByCreatedAtDesc(user))
                .thenReturn(Optional.empty()); // sem conversa anterior
        when(fitnessAiPort.chat(any(), any()))
                .thenReturn("Sim, está dentro da meta!");
        when(conversationRepository.save(any()))
                .thenAnswer(inv -> {
                    Conversation c = inv.getArgument(0);
                    c = Conversation.builder()
                            .id(1L)
                            .user(user)
                            .messages(c.getMessages())
                            .build();
                    return c;
                });

        // act
        ChatResponse response = conversationService.chat(chatRequest);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.getConversationId()).isEqualTo(1L);
        assertThat(response.getUserMessage()).isEqualTo(chatRequest.getMessage());
        assertThat(response.getAiResponse()).isEqualTo("Sim, está dentro da meta!");
        verify(conversationRepository).save(any());
    }

    @Test
    @DisplayName("deve reutilizar conversa existente")
    void chat_shouldReuseExistingConversation() {
        // arrange
        Conversation existingConversation = Conversation.builder()
                .id(1L)
                .user(user)
                .build();

        when(userRepository.findByWhatsappNumberAndIsActiveTrue("+5511999999999"))
                .thenReturn(Optional.of(user));
        when(conversationRepository.findFirstByUserOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(existingConversation));
        when(fitnessAiPort.chat(any(), any()))
                .thenReturn("Resposta da IA");
        when(conversationRepository.save(any()))
                .thenReturn(existingConversation);

        // act
        ChatResponse response = conversationService.chat(chatRequest);

        // assert
        assertThat(response.getConversationId()).isEqualTo(1L);
        verify(conversationRepository, never()).save(argThat(c ->
                c.getId() == null)); // nunca cria conversa nova
    }

    @Test
    @DisplayName("deve lançar UserNotFoundException quando usuário não encontrado")
    void chat_shouldThrowException_whenUserNotFound() {
        // arrange
        when(userRepository.findByWhatsappNumberAndIsActiveTrue(any()))
                .thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> conversationService.chat(chatRequest))
                .isInstanceOf(UserNotFoundException.class);

        verifyNoInteractions(fitnessAiPort);
        verifyNoInteractions(conversationRepository);
    }

    @Test
    @DisplayName("deve retornar histórico de conversas do usuário")
    void getHistory_shouldReturnConversations() {
        // arrange
        Conversation conversation = Conversation.builder().id(1L).user(user).build();
        ConversationResponse response = ConversationResponse.builder()
                .id(1L).userId(1L).userName("Renan").build();

        when(userRepository.findByWhatsappNumberAndIsActiveTrue("+5511999999999"))
                .thenReturn(Optional.of(user));
        when(conversationRepository.findByUserOrderByCreatedAtDesc(user))
                .thenReturn(List.of(conversation));
        when(conversationMapper.toResponseList(any()))
                .thenReturn(List.of(response));

        // act
        List<ConversationResponse> history =
                conversationService.getHistory("+5511999999999");

        // assert
        assertThat(history).hasSize(1);
        assertThat(history.get(0).getUserName()).isEqualTo("Renan");
    }

    @Test
    @DisplayName("deve retornar null quando não há conversas")
    void getLastConversation_shouldReturnNull_whenNoConversations() {
        // arrange
        when(userRepository.findByWhatsappNumberAndIsActiveTrue("+5511999999999"))
                .thenReturn(Optional.of(user));
        when(conversationRepository.findFirstByUserOrderByCreatedAtDesc(user))
                .thenReturn(Optional.empty());

        // act
        ConversationResponse result =
                conversationService.getLastConversation("+5511999999999");

        // assert
        assertThat(result).isNull();
    }
}