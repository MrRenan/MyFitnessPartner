package io.github.mrrenan.myfitnesspartner.presentation.controller;

import io.github.mrrenan.myfitnesspartner.application.service.UserService;
import io.github.mrrenan.myfitnesspartner.presentation.dto.CreateUserRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.UpdateUserRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user management operations.
 * Provides endpoints to create, retrieve, and update users.
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create new user", description = "Register a new user in the system")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("POST /users - Creating user: {}", request.getWhatsappNumber());
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/whatsapp/{whatsappNumber}")
    @Operation(summary = "Get user by WhatsApp", description = "Retrieve user information by WhatsApp number")
    public ResponseEntity<UserResponse> getUserByWhatsapp(@PathVariable String whatsappNumber) {
        log.info("GET /users/whatsapp/{} - Fetching user", whatsappNumber);
        UserResponse response = userService.findByWhatsappNumber(whatsappNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve user information by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        log.info("GET /users/{} - Fetching user", userId);
        UserResponse response = userService.findById(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/whatsapp/{whatsappNumber}")
    @Operation(summary = "Update user", description = "Update user information")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String whatsappNumber,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("PUT /users/whatsapp/{} - Updating user", whatsappNumber);
        UserResponse response = userService.updateUser(whatsappNumber, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/whatsapp/{whatsappNumber}/exists")
    @Operation(summary = "Check if user exists", description = "Check if a user exists by WhatsApp number")
    public ResponseEntity<Boolean> checkUserExists(@PathVariable String whatsappNumber) {
        log.info("GET /users/whatsapp/{}/exists - Checking user existence", whatsappNumber);
        boolean exists = userService.existsByWhatsappNumber(whatsappNumber);
        return ResponseEntity.ok(exists);
    }

    @DeleteMapping("/whatsapp/{whatsappNumber}")
    @Operation(summary = "Deactivate user", description = "Deactivate user account")
    public ResponseEntity<Void> deactivateUser(@PathVariable String whatsappNumber) {
        log.info("DELETE /users/whatsapp/{} - Deactivating user", whatsappNumber);
        userService.deactivateUser(whatsappNumber);
        return ResponseEntity.noContent().build();
    }
}