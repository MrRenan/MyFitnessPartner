package io.github.mrrenan.myfitnesspartner.presentation.controller;

import io.github.mrrenan.myfitnesspartner.application.service.MealService;
import io.github.mrrenan.myfitnesspartner.presentation.dto.CreateMealRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.MealResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for meal management.
 * Provides endpoints to register and retrieve meals.
 */
@Slf4j
@RestController
@RequestMapping("/meals")
@RequiredArgsConstructor
@Tag(name = "Meals", description = "Meal registration and tracking endpoints")
public class MealController {

    private final MealService mealService;

    @PostMapping
    @Operation(summary = "Register meal", description = "Register a new meal with pre-calculated calories and update daily goal automatically")
    public ResponseEntity<MealResponse> registerMeal(@Valid @RequestBody CreateMealRequest request) {
        log.info("POST /meals - Registering meal for: {}", request.getWhatsappNumber());
        MealResponse response = mealService.registerMeal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/from-description")
    @Operation(
            summary = "Register meal from description (AI)",
            description = "Register meal using natural language description. AI will calculate calories automatically. (Coming soon - requires Gemini AI integration)"
    )
    public ResponseEntity<MealResponse> registerMealFromDescription(
            @Valid @RequestBody io.github.mrrenan.myfitnesspartner.presentation.dto.CreateMealFromDescriptionRequest request) {
        log.info("POST /meals/from-description - AI meal registration for: {}", request.getWhatsappNumber());
        MealResponse response = mealService.registerMealFromDescription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all meals", description = "Get all meals for a user")
    public ResponseEntity<List<MealResponse>> getAllMeals(@RequestParam String whatsappNumber) {
        log.info("GET /meals - Getting all meals for: {}", whatsappNumber);
        List<MealResponse> response = mealService.getAllMeals(whatsappNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's meals", description = "Get all meals registered today")
    public ResponseEntity<List<MealResponse>> getTodaysMeals(@RequestParam String whatsappNumber) {
        log.info("GET /meals/today - Getting today's meals for: {}", whatsappNumber);
        List<MealResponse> response = mealService.getTodaysMeals(whatsappNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get meals by date", description = "Get meals for a specific date")
    public ResponseEntity<List<MealResponse>> getMealsByDate(
            @RequestParam String whatsappNumber,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /meals/date/{} - Getting meals for: {}", date, whatsappNumber);
        List<MealResponse> response = mealService.getMealsByDate(whatsappNumber, date);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/range")
    @Operation(summary = "Get meals by date range", description = "Get meals within a date range")
    public ResponseEntity<List<MealResponse>> getMealsByDateRange(
            @RequestParam String whatsappNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        log.info("GET /meals/range - Getting meals for {} from {} to {}",
                whatsappNumber, startDate, endDate);
        List<MealResponse> response = mealService.getMealsByDateRange(whatsappNumber, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{mealId}")
    @Operation(summary = "Get meal by ID", description = "Get a specific meal by ID")
    public ResponseEntity<MealResponse> getMealById(
            @PathVariable Long mealId,
            @RequestParam String whatsappNumber) {
        log.info("GET /meals/{} - Getting meal for: {}", mealId, whatsappNumber);
        MealResponse response = mealService.getMealById(mealId, whatsappNumber);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{mealId}")
    @Operation(summary = "Delete meal", description = "Delete a meal by ID")
    public ResponseEntity<Void> deleteMeal(
            @PathVariable Long mealId,
            @RequestParam String whatsappNumber) {
        log.info("DELETE /meals/{} - Deleting meal for: {}", mealId, whatsappNumber);
        mealService.deleteMeal(mealId, whatsappNumber);
        return ResponseEntity.noContent().build();
    }
}