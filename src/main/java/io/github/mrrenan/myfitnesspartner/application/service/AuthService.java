package io.github.mrrenan.myfitnesspartner.application.service;

import io.github.mrrenan.myfitnesspartner.domain.model.User;
import io.github.mrrenan.myfitnesspartner.domain.repository.UserRepository;
import io.github.mrrenan.myfitnesspartner.infrastructure.security.JwtService;
import io.github.mrrenan.myfitnesspartner.presentation.dto.AuthResponse;
import io.github.mrrenan.myfitnesspartner.presentation.dto.LoginRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registrando novo usuário: {}", request.getWhatsappNumber());

        if (userRepository.existsByWhatsappNumber(request.getWhatsappNumber())) {
            throw new IllegalArgumentException(
                    "Usuário já existe com esse número: " + request.getWhatsappNumber());
        }

        User user = User.builder()
                .name(request.getName())
                .whatsappNumber(request.getWhatsappNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .weight(request.getWeight())
                .height(request.getHeight())
                .activityLevel(request.getActivityLevel())
                .goalType(request.getGoalType())
                .isActive(true)
                .build();

        user.updateCalorieGoal();
        User saved = userRepository.save(user);

        String token = jwtService.generateToken(saved.getWhatsappNumber());
        log.info("Usuário registrado com sucesso: {}", saved.getWhatsappNumber());

        return AuthResponse.builder()
                .token(token)
                .whatsappNumber(saved.getWhatsappNumber())
                .name(saved.getName())
                .message("Usuário registrado com sucesso!")
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login: {}", request.getWhatsappNumber());

        User user = userRepository
                .findByWhatsappNumberAndIsActiveTrue(request.getWhatsappNumber())
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        String token = jwtService.generateToken(user.getWhatsappNumber());
        log.info("Login realizado com sucesso: {}", user.getWhatsappNumber());

        return AuthResponse.builder()
                .token(token)
                .whatsappNumber(user.getWhatsappNumber())
                .name(user.getName())
                .message("Login realizado com sucesso!")
                .build();
    }

    public void logout(String token) {
        jwtService.invalidateToken(token);
        log.info("Token invalidado com sucesso");
    }
}