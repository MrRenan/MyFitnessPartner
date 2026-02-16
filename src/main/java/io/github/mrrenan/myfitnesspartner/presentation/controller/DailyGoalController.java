package io.github.mrrenan.myfitnesspartner.presentation.controller;

import io.github.mrrenan.myfitnesspartner.application.service.DailyGoalService;
import io.github.mrrenan.myfitnesspartner.presentation.dto.DailyGoalResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for daily goal management.
 * Provides endpoints to track daily calorie progress.
 */
@Slf4j
@RestController
@RequestMapping("/daily-goals")
@RequiredArgsConstructor
@Tag(name = "Daily Goals", description = "Daily calorie tracking endpoints")
public class DailyGoalController {

    private final DailyGoalService dailyGoalService;

    @GetMapping("/today")
    @Operation(summary = "Get today's goal", description = "Get or create today's calorie goal for a user")
    public ResponseEntity<DailyGoalResponse> getTodaysGoal(
            @RequestParam String whatsappNumber) {
        log.info("GET /daily-goals/today - User: {}", whatsappNumber);
        DailyGoalResponse response = dailyGoalService.getTodaysGoal(whatsappNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get goal by date", description = "Get calorie goal for a specific date")
    public ResponseEntity<DailyGoalResponse> getGoalByDate(
            @RequestParam String whatsappNumber,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("GET /daily-goals/date/{} - User: {}", date, whatsappNumber);
        DailyGoalResponse response = dailyGoalService.getGoalByDate(whatsappNumber, date);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @Operation(summary = "Get goal history", description = "Get last N days of calorie tracking history")
    public ResponseEntity<List<DailyGoalResponse>> getGoalHistory(
            @RequestParam String whatsappNumber,
            @RequestParam(defaultValue = "7") int days) {
        log.info("GET /daily-goals/history - User: {}, Days: {}", whatsappNumber, days);
        List<DailyGoalResponse> response = dailyGoalService.getLastDaysGoals(whatsappNumber, days);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset today's goal", description = "Reset today's calorie count to zero")
    public ResponseEntity<DailyGoalResponse> resetTodaysGoal(
            @RequestParam String whatsappNumber) {
        log.info("POST /daily-goals/reset - User: {}", whatsappNumber);
        DailyGoalResponse response = dailyGoalService.resetTodaysGoal(whatsappNumber);
        return ResponseEntity.ok(response);
    }
}