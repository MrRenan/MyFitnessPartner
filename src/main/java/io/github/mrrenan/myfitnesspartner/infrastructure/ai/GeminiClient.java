package io.github.mrrenan.myfitnesspartner.infrastructure.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mrrenan.myfitnesspartner.infrastructure.config.GeminiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * HTTP Client for Google Gemini AI API.
 * Handles communication with Gemini REST API.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiClient {

    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1/models/";

    /**
     * Send a prompt to Gemini and get response
     */
    public String generateContent(String prompt) {
        try {
            log.debug("Sending request to Gemini API with model: {}", geminiProperties.getModel());

            // Log masked API key for debugging (show only first/last 4 chars)
            String apiKey = geminiProperties.getApiKey();
            String maskedKey = apiKey.length() > 8
                    ? apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4)
                    : "****";
            log.debug("Using API Key: {}", maskedKey);

            String url = GEMINI_API_URL + geminiProperties.getModel() + ":generateContent?key=" + geminiProperties.getApiKey();

            // Build request body according to Gemini API format
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    ),
                    "generationConfig", Map.of(
                            "temperature", geminiProperties.getTemperature(),
                            "maxOutputTokens", geminiProperties.getMaxTokens()
                    )
            );

            // Make synchronous call
            String response = webClient.post()
                    .uri(url)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("Received response from Gemini API");

            // Parse response and extract text
            return extractTextFromResponse(response);

        } catch (Exception e) {
            log.error("Error calling Gemini API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to communicate with Gemini AI: " + e.getMessage(), e);
        }
    }

    /**
     * Extract text content from Gemini API response
     */
    private String extractTextFromResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }

            log.warn("Unexpected response format from Gemini API");
            return "";

        } catch (Exception e) {
            log.error("Error parsing Gemini response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse Gemini response", e);
        }
    }
}