package io.github.mrrenan.myfitnesspartner.domain.repository;

import io.github.mrrenan.myfitnesspartner.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("ConversationRepository")
class ConversationRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired private ConversationRepository conversationRepository;
    @Autowired private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        conversationRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(User.builder()
                .name("Renan")
                .whatsappNumber("+5511999999999")
                .dateOfBirth(LocalDate.of(1994, 2, 18))
                .gender(Gender.MALE)
                .weight(93.0)
                .height(174.0)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .goalType(GoalType.LOSE_WEIGHT)
                .dailyCalorieGoal(2387)
                .isActive(true)
                .build());
    }

    @Test
    @DisplayName("deve salvar conversa com mensagens")
    void save_shouldPersistConversationWithMessages() {
        // arrange
        Conversation conversation = Conversation.builder()
                .user(user)
                .build();
        conversation.addUserMessage("Oi, qual minha meta?");
        conversation.addAssistantMessage("Sua meta é 2387 calorias!");

        // act
        Conversation saved = conversationRepository.save(conversation);

        // assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getMessages()).hasSize(2);
        assertThat(saved.getMessages().get(0).getRole()).isEqualTo("user");
        assertThat(saved.getMessages().get(1).getRole()).isEqualTo("assistant");
    }

    @Test
    @DisplayName("deve buscar conversa mais recente do usuário")
    void findFirstByUserOrderByCreatedAtDesc_shouldReturnLatestConversation() {
        // arrange
        Conversation first = conversationRepository.save(
                Conversation.builder().user(user).build());
        Conversation second = conversationRepository.save(
                Conversation.builder().user(user).build());

        // act
        Optional<Conversation> result =
                conversationRepository.findFirstByUserOrderByCreatedAtDesc(user);

        // assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(second.getId());
    }

    @Test
    @DisplayName("deve buscar todas conversas do usuário ordenadas por data")
    void findByUserOrderByCreatedAtDesc_shouldReturnAllConversations() {
        // arrange
        conversationRepository.save(Conversation.builder().user(user).build());
        conversationRepository.save(Conversation.builder().user(user).build());
        conversationRepository.save(Conversation.builder().user(user).build());

        // act
        List<Conversation> conversations =
                conversationRepository.findByUserOrderByCreatedAtDesc(user);

        // assert
        assertThat(conversations).hasSize(3);
    }

    @Test
    @DisplayName("deve retornar vazio quando usuário não tem conversas")
    void findFirstByUserOrderByCreatedAtDesc_shouldReturnEmpty_whenNoConversations() {
        // act
        Optional<Conversation> result =
                conversationRepository.findFirstByUserOrderByCreatedAtDesc(user);

        // assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("deve contar conversas do usuário corretamente")
    void countByUser_shouldReturnCorrectCount() {
        // arrange
        conversationRepository.save(Conversation.builder().user(user).build());
        conversationRepository.save(Conversation.builder().user(user).build());

        // act
        long count = conversationRepository.countByUser(user);

        // assert
        assertThat(count).isEqualTo(2);
    }
}