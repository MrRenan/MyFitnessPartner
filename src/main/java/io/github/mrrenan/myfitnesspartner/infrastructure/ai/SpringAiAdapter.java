package io.github.mrrenan.myfitnesspartner.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mrrenan.myfitnesspartner.application.dto.CalorieEstimate;
import io.github.mrrenan.myfitnesspartner.application.port.out.FitnessAiPort;
import io.github.mrrenan.myfitnesspartner.domain.exception.AiServiceUnavailableException;
import io.github.mrrenan.myfitnesspartner.infrastructure.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SpringAiAdapter implements FitnessAiPort {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public SpringAiAdapter(ChatClient.Builder builder,
                           ObjectMapper objectMapper,
                           AppProperties appProperties) {
        this.chatClient = builder
                .defaultSystem(appProperties.getAi().getSystemPrompt())
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

        String response = callAi(prompt);
        return parseCalorieEstimate(response);
    }

    @Override
    public String chat(String userMessage, String context) {
        log.info("Gerando resposta fitness para: {}", userMessage);

        String userContent = (context != null && !context.isBlank())
                ? "Contexto anterior: " + context + "\n\nMensagem: " + userMessage
                : userMessage;

        return callAi(userContent);
    }

    /**
     * Chama o modelo de IA e trata falhas do provedor (cota, timeout, indisponibilidade),
     * convertendo-as numa exceção de domínio com mensagem amigável — evita que detalhes
     * técnicos do provedor (ex: JSON de erro 429 do Gemini) vazem para o usuário.
     */
    private String callAi(String userContent) {
        try {
            return chatClient.prompt()
                    .user(userContent)
                    .call()
                    .content();
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            boolean quotaExceeded = msg.contains("429") || msg.contains("RESOURCE_EXHAUSTED");

            if (quotaExceeded) {
                log.warn("Cota da IA excedida: {}", msg);
                throw new AiServiceUnavailableException(
                        "Nosso assistente atingiu o limite de uso por hoje. " +
                        "Tente novamente mais tarde.", e);
            }

            log.error("Falha ao chamar o serviço de IA: {}", msg, e);
            throw new AiServiceUnavailableException(
                    "O assistente está indisponível no momento. Tente novamente em instantes.", e);
        }
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
            return createFallbackEstimate(response);
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
