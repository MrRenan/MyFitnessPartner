package io.github.mrrenan.myfitnesspartner.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtService")
class JwtServiceTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    private JwtService jwtService;

    private static final String SECRET =
            "myfitnesspartner-secret-key-must-be-at-least-256-bits-long";
    private static final long EXPIRATION = 86400000L; // 24h
    private static final String WHATSAPP = "+5511999999999";

    @BeforeEach
    void setUp() {
        // lenient() → esse stub pode não ser usado em todos os testes
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        jwtService = new JwtService(SECRET, EXPIRATION, redisTemplate);
    }

    @Test
    @DisplayName("deve gerar token válido para o usuário")
    void generateToken_shouldReturnValidToken() {
        // act
        String token = jwtService.generateToken(WHATSAPP);

        // assert
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    @DisplayName("deve extrair whatsappNumber corretamente do token")
    void extractWhatsappNumber_shouldReturnCorrectNumber() {
        // arrange
        String token = jwtService.generateToken(WHATSAPP);

        // act
        String extracted = jwtService.extractWhatsappNumber(token);

        // assert
        assertThat(extracted).isEqualTo(WHATSAPP);
    }

    @Test
    @DisplayName("deve validar token válido e não blacklistado")
    void isTokenValid_shouldReturnTrue_whenTokenIsValid() {
        // arrange
        String token = jwtService.generateToken(WHATSAPP);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        // act
        boolean valid = jwtService.isTokenValid(token);

        // assert
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("deve invalidar token que está na blacklist")
    void isTokenValid_shouldReturnFalse_whenTokenIsBlacklisted() {
        // arrange
        String token = jwtService.generateToken(WHATSAPP);
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        // act
        boolean valid = jwtService.isTokenValid(token);

        // assert
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("deve invalidar token malformado")
    void isTokenValid_shouldReturnFalse_whenTokenIsMalformed() {
        // act
        boolean valid = jwtService.isTokenValid("token.invalido.aqui");

        // assert
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("deve adicionar token na blacklist ao fazer logout")
    void invalidateToken_shouldAddTokenToBlacklist() {
        // arrange
        String token = jwtService.generateToken(WHATSAPP);

        // act
        jwtService.invalidateToken(token);

        // assert — verifica que salvou no Redis com tempo de expiração
        verify(valueOperations).set(
                argThat(key -> key.contains("jwt:blacklist:")),
                eq("blacklisted"),
                anyLong(),
                eq(TimeUnit.MILLISECONDS)
        );
    }
}