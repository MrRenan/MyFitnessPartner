package io.github.mrrenan.myfitnesspartner.application.service;

import io.github.mrrenan.myfitnesspartner.presentation.dto.CreateUserRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.UpdateUserRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.UserResponse;

/**
 * Service interface for user management operations.
 * Defines the contract for user-related business logic.
 */
public interface UserService {

    /**
     * Create a new user
     *
     * @param request user creation data
     * @return created user response
     * @throws IllegalArgumentException if WhatsApp number already exists
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Find user by WhatsApp number
     *
     * @param whatsappNumber WhatsApp number in format +5511999999999
     * @return user response
     * @throws UserNotFoundException if user not found
     */
    UserResponse findByWhatsappNumber(String whatsappNumber);

    /**
     * Find user by ID
     *
     * @param userId user ID
     * @return user response
     * @throws UserNotFoundException if user not found
     */
    UserResponse findById(Long userId);

    /**
     * Update user information
     *
     * @param whatsappNumber WhatsApp number
     * @param request update data
     * @return updated user response
     * @throws UserNotFoundException if user not found
     */
    UserResponse updateUser(String whatsappNumber, UpdateUserRequest request);

    /**
     * Check if user exists by WhatsApp number
     *
     * @param whatsappNumber WhatsApp number
     * @return true if user exists, false otherwise
     */
    boolean existsByWhatsappNumber(String whatsappNumber);

    /**
     * Deactivate user account
     *
     * @param whatsappNumber WhatsApp number
     * @throws UserNotFoundException if user not found
     */
    void deactivateUser(String whatsappNumber);
}