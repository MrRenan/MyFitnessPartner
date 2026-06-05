package io.github.mrrenan.myfitnesspartner.infrastructure.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mrrenan.myfitnesspartner.application.dto.CalorieEstimate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.ai.chat.client.ChatClient.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpringAiAdapter")
class SpringAiAdapterTest {

    @Mock private Builder chatClientBuilder;
    @Mock private ChatClient chatClient;
    @Mock private ChatClientRequestSpec requestSpec;
    @Mock private CallResponseSpec callResponseSpec;

    private SpringAiAdapter springAiAdapter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        when(chatClientBuilder.defaultSystem(any(String.class)))
                .thenReturn(chatClientBuilder);
        when(chatClientBuilder.build())
                .thenReturn(chatClient);

        springAiAdapter = new SpringAiAdapter(chatClientBuilder, objectMapper);
    }

    @Test
    @DisplayName("deve parsear corretamente o JSON de calorias retornado pela IA")
    void analyzeFood_shouldParseCalorieEstimateCorrectly() {
        // arrange
        String aiJsonResponse = """
                {
                  "calories": 350,
                  "protein": 28.5,
                  "carbohydrates": 30.0,
                  "fat": 12.0,
                  "explanation": "Frango grelhado com arroz integral",
                  "confidence": 0.9
                }
                """;

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(aiJsonResponse);

        // act
        CalorieEstimate result = springAiAdapter.analyzeFood("200g frango grelhado com arroz");

        // assert
        assertThat(result).isNotNull();
        assertThat(result.getCalories()).isEqualTo(350);
        assertThat(result.getProtein()).isEqualTo(28.5);
        assertThat(result.getCarbohydrates()).isEqualTo(30.0);
        assertThat(result.getFat()).isEqualTo(12.0);
        assertThat(result.getConfidence()).isEqualTo(0.9);
    }

    @Test
    @DisplayName("deve remover markdown do JSON antes de parsear")
    void analyzeFood_shouldRemoveMarkdownBeforeParsing() {
        // arrange — IA às vezes retorna com ```json
        String aiResponseWithMarkdown = """
```json
                {
                  "calories": 200,
                  "protein": 15.0,
                  "carbohydrates": 20.0,
                  "fat": 5.0,
                  "explanation": "Teste",
                  "confidence": 0.8
                }
```
                """;

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(aiResponseWithMarkdown);

        // act
        CalorieEstimate result = springAiAdapter.analyzeFood("pão com ovo");

        // assert
        assertThat(result.getCalories()).isEqualTo(200);
        assertThat(result.getProtein()).isEqualTo(15.0);
    }

    @Test
    @DisplayName("deve usar fallback quando IA retorna texto sem JSON válido")
    void analyzeFood_shouldUseFallback_whenResponseIsNotJson() {
        // arrange — IA retornou texto livre com número de calorias
        String invalidJson = "Essa refeição tem aproximadamente 450 calorias no total.";

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(invalidJson);

        // act
        CalorieEstimate result = springAiAdapter.analyzeFood("refeição qualquer");

        // assert — fallback deve extrair 450
        assertThat(result).isNotNull();
        assertThat(result.getCalories()).isEqualTo(450);
        assertThat(result.getConfidence()).isEqualTo(0.5);
    }

    @Test
    @DisplayName("deve lançar exceção quando IA retorna resposta sem nenhum número")
    void analyzeFood_shouldThrowException_whenNoCaloriesFound() {
        // arrange
        String noNumbersResponse = "Não consegui analisar essa refeição.";

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn(noNumbersResponse);

        // act & assert
        assertThatThrownBy(() -> springAiAdapter.analyzeFood("refeição inválida"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Não foi possível extrair");
    }

    @Test
    @DisplayName("deve retornar resposta do chat corretamente")
    void chat_shouldReturnAiResponse() {
        // arrange
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(any(String.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.content()).thenReturn("Sua dieta está ótima!");

        // act
        String response = springAiAdapter.chat("Como está minha dieta?", null);

        // assert
        assertThat(response).isEqualTo("Sua dieta está ótima!");
    }
}