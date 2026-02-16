package io.github.mrrenan.myfitnesspartner.application.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mrrenan.myfitnesspartner.application.dto.CalorieEstimate;
import io.github.mrrenan.myfitnesspartner.infrastructure.ai.GeminiClient;
import io.github.mrrenan.myfitnesspartner.infrastructure.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of GeminiService using Google Gemini AI.
 * Handles meal analysis and fitness-related questions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiServiceImpl implements GeminiService {

    private final GeminiClient geminiClient;
    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;

    @Override
    public CalorieEstimate calculateCaloriesFromDescription(String description) {
        log.info("Calculating calories from description: {}", description);

        try {
            String prompt = buildCalorieCalculationPrompt(description);
            String aiResponse = geminiClient.generateContent(prompt);

            log.debug("AI Response: {}", aiResponse);

            return parseCalorieEstimate(aiResponse);

        } catch (Exception e) {
            log.error("Error calculating calories with AI: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate calories with AI: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateFitnessResponse(String userMessage, String context) {
        log.info("Generating fitness response for message: {}", userMessage);

        try {
            String prompt = buildFitnessPrompt(userMessage, context);
            return geminiClient.generateContent(prompt);

        } catch (Exception e) {
            log.error("Error generating fitness response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate fitness response: " + e.getMessage(), e);
        }
    }

    /**
     * Build prompt for calorie calculation
     * Uses structured JSON output for reliable parsing
     */
    private String buildCalorieCalculationPrompt(String description) {
        return String.format("""
            Você é um nutricionista especializado. Analise a seguinte descrição de refeição e estime as calorias e macronutrientes.
            
            Descrição da refeição: "%s"
            
            Retorne APENAS um JSON válido no seguinte formato (sem markdown, sem blocos de código, apenas o JSON puro):
            {
              "calories": número inteiro de calorias totais,
              "protein": gramas de proteína (decimal),
              "carbohydrates": gramas de carboidratos (decimal),
              "fat": gramas de gordura (decimal),
              "explanation": "breve explicação do cálculo",
              "confidence": valor entre 0.0 e 1.0 indicando confiança na estimativa
            }
            
            Considere:
            - Tamanhos/quantidades mencionadas
            - Método de preparo (grelhado, frito, etc)
            - Se não houver quantidade específica, assuma porções médias
            - Seja preciso mas realista nas estimativas
            
            Retorne APENAS o JSON, sem texto adicional.
            """, description);
    }

    /**
     * Build prompt for general fitness questions
     */
    private String buildFitnessPrompt(String userMessage, String context) {
        String systemPrompt = appProperties.getAi().getSystemPrompt();

        if (context != null && !context.isBlank()) {
            return String.format("""
                %s
                
                Contexto da conversa anterior:
                %s
                
                Mensagem do usuário: %s
                
                Responda de forma clara, motivadora e baseada em evidências científicas.
                """, systemPrompt, context, userMessage);
        } else {
            return String.format("""
                %s
                
                Mensagem do usuário: %s
                
                Responda de forma clara, motivadora e baseada em evidências científicas.
                """, systemPrompt, userMessage);
        }
    }

    /**
     * Parse AI response into CalorieEstimate object
     */
    private CalorieEstimate parseCalorieEstimate(String aiResponse) {
        try {
            // Remove markdown code blocks if present
            String cleanedResponse = aiResponse
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            // Parse JSON
            JsonNode jsonNode = objectMapper.readTree(cleanedResponse);

            return CalorieEstimate.builder()
                    .calories(jsonNode.path("calories").asInt())
                    .protein(jsonNode.path("protein").asDouble())
                    .carbohydrates(jsonNode.path("carbohydrates").asDouble())
                    .fat(jsonNode.path("fat").asDouble())
                    .explanation(jsonNode.path("explanation").asText())
                    .confidence(jsonNode.path("confidence").asDouble(0.8))
                    .build();

        } catch (Exception e) {
            log.error("Error parsing AI response: {}", aiResponse, e);

            // Fallback: try to extract numbers from text
            return createFallbackEstimate(aiResponse);
        }
    }

    /**
     * Create fallback estimate if JSON parsing fails
     * Tries to extract calorie number from text
     */
    private CalorieEstimate createFallbackEstimate(String aiResponse) {
        log.warn("Using fallback calorie estimation from text: {}", aiResponse);

        // Try to find numbers that might be calories (typically 100-3000 range)
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
                // Continue searching
            }
        }

        // If nothing found, throw exception
        throw new RuntimeException("Could not extract calorie information from AI response");
    }
}