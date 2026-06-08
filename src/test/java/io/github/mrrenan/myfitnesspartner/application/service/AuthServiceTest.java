package io.github.mrrenan.myfitnesspartner.application.service;

import io.github.mrrenan.myfitnesspartner.domain.model.ActivityLevel;
import io.github.mrrenan.myfitnesspartner.domain.model.Gender;
import io.github.mrrenan.myfitnesspartner.domain.model.GoalType;
import io.github.mrrenan.myfitnesspartner.domain.model.User;
import io.github.mrrenan.myfitnesspartner.domain.repository.UserRepository;
import io.github.mrrenan.myfitnesspartner.infrastructure.security.JwtService;
import io.github.mrrenan.myfitnesspartner.presentation.dto.AuthResponse;
import io.github.mrrenan.myfitnesspartner.presentation.dto.LoginRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .name("Renan")
                .whatsappNumber("+5511999999999")
                .password("minhasenha123")
                .dateOfBirth(LocalDate.of(1994, 2, 18))
                .gender(Gender.MALE)
                .weight(93.0)
                .height(174.0)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .goalType(GoalType.LOSE_WEIGHT)
                .build();

        loginRequest = LoginRequest.builder()
                .whatsappNumber("+5511999999999")
                .password("minhasenha123")
                .build();

        user = User.builder()
                .id(1L)
                .name("Renan")
                .whatsappNumber("+5511999999999")
                .password("$2a$10$hashedpassword")
                .dateOfBirth(LocalDate.of(1994, 2, 18))
                .gender(Gender.MALE)
                .weight(93.0)
                .height(174.0)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .goalType(GoalType.LOSE_WEIGHT)
                .dailyCalorieGoal(2387)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("deve registrar novo usuário e retornar token")
    void register_shouldCreateUserAndReturnToken() {
        // arrange
        when(userRepository.existsByWhatsappNumber("+5511999999999"))
                .thenReturn(false);
        when(passwordEncoder.encode("minhasenha123"))
                .thenReturn("$2a$10$hashedpassword");
        when(userRepository.save(any(User.class)))
                .thenReturn(user);
        when(jwtService.generateToken("+5511999999999"))
                .thenReturn("jwt-token-gerado");

        // act
        AuthResponse response = authService.register(registerRequest);

        // assert
        assertThat(response.getToken()).isEqualTo("jwt-token-gerado");
        assertThat(response.getWhatsappNumber()).isEqualTo("+5511999999999");
        assertThat(response.getName()).isEqualTo("Renan");
        assertThat(response.getMessage()).contains("sucesso");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("minhasenha123");
    }

    @Test
    @DisplayName("deve lançar exceção ao registrar usuário já existente")
    void register_shouldThrowException_whenUserAlreadyExists() {
        // arrange
        when(userRepository.existsByWhatsappNumber("+5511999999999"))
                .thenReturn(true);

        // act & assert
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já existe");

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("deve fazer login e retornar token")
    void login_shouldReturnToken_whenCredentialsAreValid() {
        // arrange
        when(userRepository.findByWhatsappNumberAndIsActiveTrue("+5511999999999"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("minhasenha123", "$2a$10$hashedpassword"))
                .thenReturn(true);
        when(jwtService.generateToken("+5511999999999"))
                .thenReturn("jwt-token-gerado");

        // act
        AuthResponse response = authService.login(loginRequest);

        // assert
        assertThat(response.getToken()).isEqualTo("jwt-token-gerado");
        assertThat(response.getWhatsappNumber()).isEqualTo("+5511999999999");
        verify(jwtService).generateToken("+5511999999999");
    }

    @Test
    @DisplayName("deve lançar exceção quando usuário não encontrado no login")
    void login_shouldThrowException_whenUserNotFound() {
        // arrange
        when(userRepository.findByWhatsappNumberAndIsActiveTrue(anyString()))
                .thenReturn(Optional.empty());

        // act & assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciais inválidas");

        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("deve lançar exceção quando senha incorreta")
    void login_shouldThrowException_whenPasswordIsWrong() {
        // arrange
        when(userRepository.findByWhatsappNumberAndIsActiveTrue("+5511999999999"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        // act & assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Credenciais inválidas");

        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("deve chamar invalidateToken ao fazer logout")
    void logout_shouldInvalidateToken() {
        // act
        authService.logout("jwt-token");

        // assert
        verify(jwtService).invalidateToken("jwt-token");
    }
}