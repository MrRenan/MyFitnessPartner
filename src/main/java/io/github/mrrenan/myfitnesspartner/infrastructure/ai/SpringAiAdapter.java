package io.github.mrrenan.myfitnesspartner.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mrrenan.myfitnesspartner.application.dto.CalorieEstimate;
import io.github.mrrenan.myfitnesspartner.application.port.out.FitnessAiPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpringAiAdapter implements FitnessAiPort {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public SpringAiAdapter(ChatClient.Builder builder, ObjectMapper objectMapper) {
        this.chatClient = builder
                .defaultSystem("Você é um assistente fitness. Responda sempre em português.")
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public CalorieEstimate analyzeFood(String description) {
        log.info("Analisando refeição com IA: {}", description);

        String prompt = """
            Analise a refeição e retorne APENAS JSON válido, sem markdown:
            {
              "calories": int,
              "protein": double,
              "carbohydrates": double,
              "fat": double,
              "explanation": "string",
              "confidence": double
            }
            Refeição: "%s"
            """.formatted(description);

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return parseCalorieEstimate(response);
    }

    @Override
    public String chat(String userMessage, String context) {
        log.info("Gerando resposta fitness para: {}", userMessage);

        String userContent = (context != null && !context.isBlank())
                ? "Contexto anterior: " + context + "\n\nMensagem: " + userMessage
                : userMessage;

        return chatClient.prompt()
                .user(userContent)
                .call()
                .content();
    }

    private CalorieEstimate parseCalorieEstimate(String response) {
        try {
            String clean = response.replaceAll("```json|```", "").trim();
            JsonNode node = objectMapper.readTree(clean);

            return CalorieEstimate.builder()
                    .calories(node.path("calories").asInt())
                    .protein(node.path("protein").asDouble())
                    .carbohydrates(node.path("carbohydrates").asDouble())
                    .fat(node.path("fat").asDouble())
                    .explanation(node.path("explanation").asText())
                    .confidence(node.path("confidence").asDouble(0.8))
                    .build();

        } catch (Exception e) {
            log.error("Erro ao parsear resposta da IA: {}", response, e);
            return createFallbackEstimate(response); // ← aproveita o fallback
        }
    }

    private CalorieEstimate createFallbackEstimate(String aiResponse) {
        log.warn("Usando fallback para estimar calorias do texto: {}", aiResponse);

        String[] words = aiResponse.split("\\s+");
        for (String word : words) {
            try {
                int number = Integer.parseInt(word.replaceAll("[^0-9]", ""));
                if (number >= 50 && number <= 3000) {
                    return CalorieEstimate.builder()
                            .calories(number)
                            .protein(0.0)
                            .carbohydrates(0.0)
                            .fat(0.0)
                            .explanation("Estimativa baseada em análise de texto (sem detalhes de macros)")
                            .confidence(0.5)
                            .build();
                }
            } catch (NumberFormatException ignored) {
                // continua buscando
            }
        }

        throw new RuntimeException("Não foi possível extrair informações calóricas da resposta da IA");
    }
}
