package io.github.mrrenan.myfitnesspartner.infrastructure.whatsapp;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.github.mrrenan.myfitnesspartner.infrastructure.config.TwilioProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Responsável por enviar mensagens via Twilio WhatsApp Sandbox.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WhatsAppMessageSender {

    private final TwilioProperties twilioProperties;

    @PostConstruct
    public void init() {
        // Log para verificar as credenciais carregadas (mostra só os primeiros caracteres)
        log.info("Twilio Account SID: {}...",
                twilioProperties.getAccountSid().substring(0, 8));
        log.info("Twilio Auth Token: {}...",
                twilioProperties.getAuthToken().substring(0, 4));
        log.info("Twilio WhatsApp Number: {}",
                twilioProperties.getWhatsappNumber());

        Twilio.init(
                twilioProperties.getAccountSid(),
                twilioProperties.getAuthToken()
        );
        log.info("Twilio inicializado com sucesso");
    }

    /**
     * Envia uma mensagem de texto via WhatsApp
     *
     * @param to      número do destinatário (formato: +5511999999999)
     * @param message texto da mensagem
     */
    public void sendMessage(String to, String message) {
        try {
            // Twilio exige o prefixo "whatsapp:" no número
            String toWhatsApp = to.startsWith("whatsapp:") ? to : "whatsapp:" + to;

            Message twilioMessage = Message.creator(
                    new PhoneNumber(toWhatsApp),
                    new PhoneNumber(twilioProperties.getWhatsappNumber()),
                    message
            ).create();

            log.info("Mensagem enviada para {} — SID: {}", to, twilioMessage.getSid());

        } catch (Exception e) {
            log.error("Erro ao enviar mensagem para {}: {}", to, e.getMessage());
            throw new RuntimeException("Falha ao enviar mensagem WhatsApp: " + e.getMessage(), e);
        }
    }
}