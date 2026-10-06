package io.github.mrrenan.myfitnesspartner.application.service.onboarding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mrrenan.myfitnesspartner.domain.model.*;
import io.github.mrrenan.myfitnesspartner.domain.repository.UserRepository;
import io.github.mrrenan.myfitnesspartner.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gerencia o fluxo conversacional de onboarding.
 *
 * Fluxo:
 * 1. Usuário envia primeira mensagem com número + senha
 * 2. IA conduz conversa coletando: nome, data nascimento, gênero, peso, altura, nível atividade, objetivo
 * 3. Quando todos os dados estão coletados, a IA retorna JSON estruturado
 * 4. Service cria o usuário e retorna o token JWT
 *
 * Estado da sessão fica em memória (ConcurrentHashMap) — suficiente para MVP.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final ChatClient.Builder chatClientBuilder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    // Sessões em memória: chave = phoneNumber
    private final Map<String, OnboardingSession> sessions = new ConcurrentHashMap<>();

    // System prompt específico para onboarding — diferente do prompt de fitness
    private static final String ONBOARDING_SYSTEM_PROMPT = """
            Você é um assistente de cadastro do MyFitnessPartner, um app de acompanhamento fitness.
            Sua tarefa é coletar os dados do usuário de forma natural e amigável, como uma conversa.
            
            Dados que você precisa coletar (nessa ordem aproximada):
            1. Nome completo
            2. Data de nascimento (formato DD/MM/AAAA)
            3. Sexo biológico (para cálculo de metabolismo)
            4. Peso atual em kg
            5. Altura em cm
            6. Nível de atividade física
            7. Objetivo (perder peso, manter ou ganhar)
            
            Regras:
            - Colete um ou dois dados por mensagem, não sobrecarregue o usuário
            - Seja simpático e motivador, com uma abordagem de parceria, use emojis com moderação
            - Se o usuário responder de forma ambígua, peça esclarecimento
            - Confirme os dados antes de finalizar
            
            Quando tiver TODOS os 7 dados confirmados, retorne APENAS este JSON (sem texto antes ou depois):
            {
              "onboarding_complete": true,
              "name": "nome completo",
              "date_of_birth": "YYYY-MM-DD",
              "gender": "MALE ou FEMALE",
              "weight": 70.5,
              "height": 175.0,
              "activity_level": "SEDENTARY, LIGHTLY_ACTIVE, MODERATELY_ACTIVE, VERY_ACTIVE ou EXTREMELY_ACTIVE",
              "goal_type": "LOSE_WEIGHT, MAINTAIN_WEIGHT ou GAIN_WEIGHT",
              "message": "mensagem de boas-vindas personalizada para o usuário"
            }
            
            Enquanto ainda estiver coletando dados, responda normalmente em texto.
            """;

    /**
     * Processa uma mensagem de onboarding.
     *
     * @param phoneNumber número de telefone (identificador da sessão)
     * @param password    senha escolhida pelo usuário
     * @param message     mensagem enviada pelo usuário
     */
    @Transactional
    public OnboardingResponse chat(String phoneNumber, String password, String message) {
        // Limpa sessões expiradas periodicamente
        cleanExpiredSessions();

        // Verifica se usuário já existe
        if (userRepository.existsByWhatsappNumber(phoneNumber)) {
            // Mascara o número para não expor PII em logs de produção
            log.warn("Tentativa de onboarding para número já cadastrado: {}***",
                    phoneNumber.substring(0, Math.min(6, phoneNumber.length())));
            return OnboardingResponse.builder()
                    .status(OnboardingResponse.Status.ERROR)
                    .aiMessage("Esse número já está cadastrado. Faça login para continuar.")
                    .build();
        }

        // Recupera ou cria sessão
        OnboardingSession session = sessions.computeIfAbsent(
                phoneNumber,
                k -> new OnboardingSession(phoneNumber, password)
        );

        // Chama a IA com o system prompt de onboarding.
        // O histórico já salvo é o contexto; a mensagem atual vai separada no user().
        // Só adicionamos ao histórico APÓS a IA responder com sucesso — evita
        // desalinhamento em caso de retry por erro transiente (503, timeout etc).
        ChatClient chatClient = chatClientBuilder
                .defaultSystem(ONBOARDING_SYSTEM_PROMPT)
                .build();

        String history = session.buildContext();
        String userContent = history.isBlank()
                ? message
                : "Histórico da conversa:\n" + history + "\n\nUsuário: " + message;

        String aiResponse = chatClient.prompt()
                .user(userContent)
                .call()
                .content();

        log.debug("Resposta da IA para onboarding de {}: {}", phoneNumber, aiResponse);

        // Só persiste no histórico após resposta bem-sucedida
        session.addUserMessage(message);

        // Verifica se a IA retornou o JSON de conclusão
        OnboardingCompletion completion = tryParseCompletion(aiResponse);

        if (completion != null) {
            // Dados coletados — cria o usuário
            return createUser(session, completion);
        }

        // Ainda em andamento — adiciona resposta da IA e continua
        session.addAssistantMessage(aiResponse);

        return OnboardingResponse.builder()
                .status(OnboardingResponse.Status.ONBOARDING)
                .aiMessage(aiResponse)
                .build();
    }

    /**
     * Tenta fazer parse do JSON de conclusão retornado pela IA.
     * Retorna null se a resposta não for um JSON de conclusão.
     */
    private OnboardingCompletion tryParseCompletion(String aiResponse) {
        try {
            // Remove markdown code blocks se presentes
            String clean = aiResponse.replaceAll("```json|```", "").trim();

            // Só tenta parsear se parecer JSON
            if (!clean.startsWith("{")) return null;

            JsonNode node = objectMapper.readTree(clean);

            if (!node.path("onboarding_complete").asBoolean(false)) return null;

            return new OnboardingCompletion(
                    node.path("name").asText(),
                    LocalDate.parse(node.path("date_of_birth").asText(), DateTimeFormatter.ISO_LOCAL_DATE),
                    Gender.valueOf(node.path("gender").asText()),
                    node.path("weight").asDouble(),
                    node.path("height").asDouble(),
                    ActivityLevel.valueOf(node.path("activity_level").asText()),
                    GoalType.valueOf(node.path("goal_type").asText()),
                    node.path("message").asText()
            );
        } catch (Exception e) {
            log.debug("Resposta da IA não é JSON de conclusão: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Cria o usuário no banco e retorna o token JWT.
     */
    private OnboardingResponse createUser(OnboardingSession session, OnboardingCompletion completion) {
        try {
            User user = User.builder()
                    .name(completion.name())
                    .whatsappNumber(session.getPhoneNumber())
                    .password(passwordEncoder.encode(session.getPassword()))
                    .dateOfBirth(completion.dateOfBirth())
                    .gender(completion.gender())
                    .weight(completion.weight())
                    .height(completion.height())
                    .activityLevel(completion.activityLevel())
                    .goalType(completion.goalType())
                    .isActive(true)
                    .build();

            user.updateCalorieGoal();
            User saved = userRepository.save(user);

            // Remove sessão da memória
            sessions.remove(session.getPhoneNumber());

            String token = jwtService.generateToken(saved.getWhatsappNumber());
            log.info("Usuário criado via onboarding: {} ({})", saved.getName(), saved.getWhatsappNumber());

            return OnboardingResponse.builder()
                    .status(OnboardingResponse.Status.COMPLETED)
                    .aiMessage(completion.welcomeMessage())
                    .token(token)
                    .whatsappNumber(saved.getWhatsappNumber())
                    .name(saved.getName())
                    .build();

        } catch (Exception e) {
            log.error("Erro ao criar usuário no onboarding: {}", e.getMessage(), e);
            return OnboardingResponse.builder()
                    .status(OnboardingResponse.Status.ERROR)
                    .aiMessage("Ocorreu um erro ao finalizar seu cadastro. Tente novamente.")
                    .build();
        }
    }

    private void cleanExpiredSessions() {
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * Record interno para os dados extraídos do JSON da IA.
     */
    private record OnboardingCompletion(
            String name,
            LocalDate dateOfBirth,
            Gender gender,
            double weight,
            double height,
            ActivityLevel activityLevel,
            GoalType goalType,
            String welcomeMessage
    ) {}
}
