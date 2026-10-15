package com.apibotmessage.controller;

import com.apibotmessage.domain.dto.webhook.MetaWebhookEvent;
import com.apibotmessage.service.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final MessageProcessor messageProcessor;
    private final String expectedVerifyToken;

    public WebhookController(MessageProcessor messageProcessor,
                             @Value("${whatsapp.verify-token}") String expectedVerifyToken) {
        this.messageProcessor = messageProcessor;
        this.expectedVerifyToken = expectedVerifyToken;
    }

    /**
     * GET /webhook - Validação da Meta (hub.challenge)
     * Retorna STRING PURA (o challenge) se token válido, senão 403.
     */
    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam(name = "hub.mode") String mode,
            @RequestParam(name = "hub.verify_token") String verifyToken,
            @RequestParam(name = "hub.challenge") String challenge) {

        log.debug("Verificação webhook: mode={}, token={}", mode, maskToken(verifyToken));

        if (!"subscribe".equals(mode)) {
            log.warn("hub.mode inválido: {}", mode);
            return ResponseEntity.status(403).body("Invalid mode");
        }

        if (!expectedVerifyToken.equals(verifyToken)) {
            log.warn("Verify token inválido. Esperado: {}, Recebido: {}",
                    maskToken(expectedVerifyToken), maskToken(verifyToken));
            return ResponseEntity.status(403).body("Invalid verify token");
        }

        log.info("Webhook verificado com sucesso");
        // RETORNA STRING PURA - NÃO JSON, NÃO OBJETO
        return ResponseEntity.ok(challenge);
    }

    /**
     * POST /webhook - Recebe eventos da Meta
     * Retorna 200 OK IMEDIATAMENTE (requisito da Meta: < 20s, ideal < 1s)
     * Processamento real acontece em thread assíncrona.
     */
    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody MetaWebhookEvent event) {
        // Log mínimo para auditoria
        log.debug("Webhook POST recebido: object={}, entries={}",
                event.getObject(), event.getEntries() != null ? event.getEntries().size() : 0);

        // Delega para processamento assíncrono (não bloqueia a thread do Tomcat)
        messageProcessor.process(event);

        // Retorno IMEDIATO - Meta exige 2xx em < 20s
        return ResponseEntity.ok().build();
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 4) return "***";
        return token.substring(0, 2) + "***" + token.substring(token.length() - 2);
    }
}