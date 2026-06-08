package io.github.mrrenan.myfitnesspartner.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mrrenan.myfitnesspartner.domain.model.ActivityLevel;
import io.github.mrrenan.myfitnesspartner.domain.model.Gender;
import io.github.mrrenan.myfitnesspartner.domain.model.GoalType;
import io.github.mrrenan.myfitnesspartner.presentation.dto.LoginRequest;
import io.github.mrrenan.myfitnesspartner.presentation.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("AuthController")
class AuthControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    private RegisterRequest buildRegisterRequest(String whatsappNumber) {
        return RegisterRequest.builder()
                .name("Renan")
                .whatsappNumber(whatsappNumber)
                .password("minhasenha123")
                .dateOfBirth(LocalDate.of(1994, 2, 18))
                .gender(Gender.MALE)
                .weight(93.0)
                .height(174.0)
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .goalType(GoalType.LOSE_WEIGHT)
                .build();
    }

    @Test
    @DisplayName("deve registrar usuário e retornar token")
    void register_shouldReturnToken() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildRegisterRequest("+5511911111111"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.whatsappNumber").value("+5511911111111"))
                .andExpect(jsonPath("$.name").value("Renan"));
    }

    @Test
    @DisplayName("deve fazer login e retornar token")
    void login_shouldReturnToken() throws Exception {
        // arrange — registra primeiro
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildRegisterRequest("+5511922222222"))))
                .andExpect(status().isCreated());

        // act — faz login
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                LoginRequest.builder()
                                        .whatsappNumber("+5511922222222")
                                        .password("minhasenha123")
                                        .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.message").value("Login realizado com sucesso!"));
    }

    @Test
    @DisplayName("deve retornar 400 ao tentar registrar usuário duplicado")
    void register_shouldReturn400_whenUserAlreadyExists() throws Exception {
        // arrange — registra primeiro
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildRegisterRequest("+5511933333333"))))
                .andExpect(status().isCreated());

        // act — tenta registrar de novo
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildRegisterRequest("+5511933333333"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("deve retornar 401 ao acessar endpoint protegido sem token")
    void protectedEndpoint_shouldReturn403_whenNoToken() throws Exception {
        mockMvc.perform(get("/conversations/history")
                        .param("whatsappNumber", "+5511944444444"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("deve fazer logout e invalidar token")
    void logout_shouldInvalidateToken() throws Exception {
        // arrange — registra e pega o token
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildRegisterRequest("+5511955555555"))))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();

        // act — faz logout
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout realizado com sucesso!"));

        // assert — token invalidado, não acessa mais
        mockMvc.perform(get("/conversations/history")
                        .param("whatsappNumber", "+5511955555555")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}