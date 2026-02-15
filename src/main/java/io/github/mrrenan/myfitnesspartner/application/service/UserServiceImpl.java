package io.github.mrrenan.myfitnesspartner.application.service;

import io.github.mrrenan.myfitnesspartner.domain.exception.UserNotFoundException;
import io.github.mrrenan.myfitnesspartner.domain.model.User;
import io.github.mrrenan.myfitnesspartner.domain.repository.UserRepository;
import io.github.mrrenan.myfitnesspartner.presentation.dto.CreateUserRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.UpdateUserRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.UserResponse;
import io.github.mrrenan.myfitnesspartner.presentation.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of UserService.
 * Handles all user-related business logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating new user with WhatsApp: {}", request.getWhatsappNumber());

        // Validate that WhatsApp number doesn't already exist
        if (userRepository.existsByWhatsappNumber(request.getWhatsappNumber())) {
            log.warn("Attempt to create user with existing WhatsApp: {}", request.getWhatsappNumber());
            throw new IllegalArgumentException(
                    "User with WhatsApp number " + request.getWhatsappNumber() + " already exists"
            );
        }

        // Convert DTO to Entity
        User user = userMapper.toEntity(request);

        // Save to database
        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {} and daily calorie goal: {}",
                savedUser.getId(), savedUser.getDailyCalorieGoal());

        // Convert Entity to Response DTO
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findByWhatsappNumber(String whatsappNumber) {
        log.debug("Finding user by WhatsApp: {}", whatsappNumber);

        User user = userRepository.findByWhatsappNumberAndIsActiveTrue(whatsappNumber)
                .orElseThrow(() -> {
                    log.warn("User not found with WhatsApp: {}", whatsappNumber);
                    return new UserNotFoundException(whatsappNumber);
                });

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long userId) {
        log.debug("Finding user by ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new UserNotFoundException(userId);
                });

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String whatsappNumber, UpdateUserRequest request) {
        log.info("Updating user with WhatsApp: {}", whatsappNumber);

        // Find existing user
        User user = userRepository.findByWhatsappNumberAndIsActiveTrue(whatsappNumber)
                .orElseThrow(() -> {
                    log.warn("User not found with WhatsApp: {}", whatsappNumber);
                    return new UserNotFoundException(whatsappNumber);
                });

        // Store old calorie goal for logging
        Integer oldCalorieGoal = user.getDailyCalorieGoal();

        // Update entity with new data
        userMapper.updateEntity(user, request);

        // Save changes
        User updatedUser = userRepository.save(user);

        // Log if calorie goal changed
        if (!oldCalorieGoal.equals(updatedUser.getDailyCalorieGoal())) {
            log.info("User {} calorie goal updated from {} to {}",
                    whatsappNumber, oldCalorieGoal, updatedUser.getDailyCalorieGoal());
        }

        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByWhatsappNumber(String whatsappNumber) {
        log.debug("Checking if user exists with WhatsApp: {}", whatsappNumber);
        return userRepository.existsByWhatsappNumber(whatsappNumber);
    }

    @Override
    @Transactional
    public void deactivateUser(String whatsappNumber) {
        log.info("Deactivating user with WhatsApp: {}", whatsappNumber);

        User user = userRepository.findByWhatsappNumber(whatsappNumber)
                .orElseThrow(() -> {
                    log.warn("User not found with WhatsApp: {}", whatsappNumber);
                    return new UserNotFoundException(whatsappNumber);
                });

        user.setIsActive(false);
        userRepository.save(user);

        log.info("User {} deactivated successfully", whatsappNumber);
    }
}