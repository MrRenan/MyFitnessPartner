package io.github.mrrenan.myfitnesspartner.application.service;

import io.github.mrrenan.myfitnesspartner.domain.exception.UserNotFoundException;
import io.github.mrrenan.myfitnesspartner.domain.model.*;
import io.github.mrrenan.myfitnesspartner.domain.repository.UserRepository;
import io.github.mrrenan.myfitnesspartner.presentation.dto.*;
import io.github.mrrenan.myfitnesspartner.presentation.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl")
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Renan")
                .whatsappNumber("+5511999999999")
                .password("senha")
                .dateOfBirth(LocalDate.of(1994, 2, 18))
                .gender(Gender.MALE)
                .weight(93.0)
                .height(174.0)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .goalType(GoalType.LOSE_WEIGHT)
                .dailyCalorieGoal(2387)
                .isActive(true)
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .name("Renan")
                .whatsappNumber("+5511999999999")
                .dailyCalorieGoal(2387)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("deve criar usuário com sucesso")
    void createUser_shouldSaveAndReturnUser() {
        // arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .name("Renan")
                .whatsappNumber("+5511999999999")
                .dateOfBirth(LocalDate.of(1994, 2, 18))
                .gender(Gender.MALE)
                .weight(93.0)
                .height(174.0)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .goalType(GoalType.LOSE_WEIGHT)
                .build();

        when(userRepository.existsByWhatsappNumber("+5511999999999"))
                .thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // act
        UserResponse response = userService.createUser(request);

        // assert
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Renan");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("deve lançar exceção ao criar usuário com WhatsApp duplicado")
    void createUser_shouldThrowException_whenWhatsappAlreadyExists() {
        // arrange
        CreateUserRequest request = CreateUserRequest.builder()
                .whatsappNumber("+5511999999999")
                .build();

        when(userRepository.existsByWhatsappNumber("+5511999999999"))
                .thenReturn(true);

        // act & assert
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve buscar usuário por WhatsApp")
    void findByWhatsappNumber_shouldReturnUser() {
        // arrange
        when(userRepository.findByWhatsappNumberAndIsActiveTrue("+5511999999999"))
                .thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // act
        UserResponse response = userService.findByWhatsappNumber("+5511999999999");

        // assert
        assertThat(response).isNotNull();
        assertThat(response.getWhatsappNumber()).isEqualTo("+5511999999999");
    }

    @Test
    @DisplayName("deve lançar exceção quando usuário não encontrado por WhatsApp")
    void findByWhatsappNumber_shouldThrowException_whenNotFound() {
        // arrange
        when(userRepository.findByWhatsappNumberAndIsActiveTrue(anyString()))
                .thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() ->
                userService.findByWhatsappNumber("+5511999999999"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("deve atualizar usuário com sucesso")
    void updateUser_shouldUpdateAndReturnUser() {
        // arrange
        UpdateUserRequest request = UpdateUserRequest.builder()
                .weight(90.0)
                .goalType(GoalType.MAINTAIN_WEIGHT)
                .build();

        when(userRepository.findByWhatsappNumberAndIsActiveTrue("+5511999999999"))
                .thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // act
        UserResponse response = userService.updateUser("+5511999999999", request);

        // assert
        assertThat(response).isNotNull();
        verify(userMapper).updateEntity(user, request);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("deve desativar usuário")
    void deactivateUser_shouldSetIsActiveFalse() {
        // arrange
        when(userRepository.findByWhatsappNumber("+5511999999999"))
                .thenReturn(Optional.of(user));

        // act
        userService.deactivateUser("+5511999999999");

        // assert
        verify(userRepository).save(argThat(u -> !u.getIsActive()));
    }

    @Test
    @DisplayName("deve verificar se usuário existe")
    void existsByWhatsappNumber_shouldReturnTrue_whenExists() {
        // arrange
        when(userRepository.existsByWhatsappNumber("+5511999999999"))
                .thenReturn(true);

        // act
        boolean exists = userService.existsByWhatsappNumber("+5511999999999");

        // assert
        assertThat(exists).isTrue();
    }
}