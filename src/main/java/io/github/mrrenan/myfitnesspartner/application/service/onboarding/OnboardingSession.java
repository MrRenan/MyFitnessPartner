package io.github.mrrenan.myfitnesspartner.application.service.onboarding;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * Estado em memória de uma sessão de onboarding.
 * Existe apenas enquanto o usuário ainda não foi criado no banco.
 * Identificada pelo número de telefone + senha.
 *
 * A senha é mantida em texto claro intencionalmente — ela precisa ser
 * codificada com BCrypt apenas no momento de persistir o usuário.
 * A sessão expira em 30 minutos sem atividade.
 */
@Data
public class OnboardingSession {

    // Identificadores da sessão
    private final String phoneNumber;
    private final String password;

    // Histórico de mensagens da conversa de onboarding (thread-safe)
    private final List<Message> messages = Collections.synchronizedList(new ArrayList<>());

    // Controle de expiração — sessão expira em 30 minutos sem atividade
    private volatile LocalDateTime lastActivity = LocalDateTime.now();

    public void addUserMessage(String content) {
        messages.add(new Message("user", content));
        lastActivity = LocalDateTime.now();
    }

    public void addAssistantMessage(String content) {
        messages.add(new Message("assistant", content));
        lastActivity = LocalDateTime.now();
    }

    /**
     * Sessão expirada se não houve atividade nos últimos 30 minutos.
     */
    public boolean isExpired() {
        return lastActivity.isBefore(LocalDateTime.now().minusMinutes(30));
    }

    /**
     * Monta o contexto das últimas mensagens para enviar para a IA.
     */
    public String buildContext() {
        if (messages.isEmpty()) return "";

        synchronized (messages) {
            int start = Math.max(0, messages.size() - 10);
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < messages.size(); i++) {
                Message m = messages.get(i);
                sb.append(m.role().toUpperCase()).append(": ").append(m.content()).append("\n");
            }
            return sb.toString().trim();
        }
    }

    public record Message(String role, String content) {}
}
