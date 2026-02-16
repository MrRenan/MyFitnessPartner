package io.github.mrrenan.myfitnesspartner.presentation.controller;

import io.github.mrrenan.myfitnesspartner.application.dto.CalorieEstimate;
import io.github.mrrenan.myfitnesspartner.application.service.GeminiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for AI operations.
 * Provides endpoints to test AI functionality directly.
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "AI-powered endpoints for testing")
public class AIController {

    private final GeminiService geminiService;

    @PostMapping("/calculate-calories")
    @Operation(
            summary = "Calculate calories from description",
            description = "Use AI to analyze meal description and estimate calories and macros"
    )
    public ResponseEntity<CalorieEstimate> calculateCalories(@RequestBody Map<String, String> request) {
        String description = request.get("description");
        log.info("POST /ai/calculate-calories - Description: {}", description);

        CalorieEstimate estimate = geminiService.calculateCaloriesFromDescription(description);
        return ResponseEntity.ok(estimate);
    }

    @PostMapping("/fitness-question")
    @Operation(
            summary = "Ask fitness question",
            description = "Ask a general fitness, diet, or workout question to the AI"
    )
    public ResponseEntity<Map<String, String>> askFitnessQuestion(@RequestBody Map<String, String> request) {
        String question = request.get("question");
        String context = request.get("context");
        log.info("POST /ai/fitness-question - Question: {}", question);

        String response = geminiService.generateFitnessResponse(question, context);
        return ResponseEntity.ok(Map.of("response", response));
    }
}