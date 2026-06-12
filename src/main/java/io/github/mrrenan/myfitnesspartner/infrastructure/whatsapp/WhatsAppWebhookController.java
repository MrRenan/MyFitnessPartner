package io.github.mrrenan.myfitnesspartner.infrastructure.whatsapp;

import io.github.mrrenan.myfitnesspartner.infrastructure.whatsapp.dto.WhatsAppWebhookPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller do webhook Twilio WhatsApp Sandbox.
 *
 * POST /webhook/whatsapp → recebe mensagens dos usuários via Twilio
 */
@Slf4j
@RestController
@RequestMapping("/webhook/whatsapp")
@RequiredArgsConstructor
public class WhatsAppWebhookController {

    private final WhatsAppWebhookHandler webhookHandler;

    /**
     * Recebe mensagens do Twilio via form-urlencoded.
     * Retorna TwiML vazio — a resposta é enviada de forma assíncrona.
     */
    @PostMapping(
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE
    )
    public ResponseEntity<String> receiveMessage(
            @RequestParam("Body") String body,
            @RequestParam("From") String from) {

        log.info("Mensagem recebida de {}: {}", from, body);

        // Monta payload e processa de forma assíncrona
        // Twilio tem timeout de 15s — retornamos imediatamente
        WhatsAppWebhookPayload payload = buildPayload(from, body);
        webhookHandler.handle(payload);

        // TwiML vazio — resposta real é enviada via Twilio API
        return ResponseEntity.ok("<Response></Response>");
    }

    /**
     * Adapta os parâmetros do Twilio para o payload padrão
     */
    private WhatsAppWebhookPayload buildPayload(String from, String body) {
        WhatsAppWebhookPayload.TextBody text = new WhatsAppWebhookPayload.TextBody();
        text.setBody(body);

        WhatsAppWebhookPayload.Message message = new WhatsAppWebhookPayload.Message();
        message.setFrom(from.replace("whatsapp:+", "")); // remove prefixo whatsapp:+
        message.setType("text");
        message.setText(text);

        WhatsAppWebhookPayload.Value value = new WhatsAppWebhookPayload.Value();
        value.setMessages(java.util.List.of(message));

        WhatsAppWebhookPayload.Change change = new WhatsAppWebhookPayload.Change();
        change.setValue(value);

        WhatsAppWebhookPayload.Entry entry = new WhatsAppWebhookPayload.Entry();
        entry.setChanges(java.util.List.of(change));

        WhatsAppWebhookPayload payload = new WhatsAppWebhookPayload();
        payload.setObject("whatsapp_business_account");
        payload.setEntry(java.util.List.of(entry));

        return payload;
    }
}