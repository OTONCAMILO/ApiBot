package com.apibotmessage.service.impl;

import com.apibotmessage.domain.dto.webhook.MetaMessage;
import com.apibotmessage.domain.dto.webhook.MetaWebhookEvent;
import com.apibotmessage.domain.enums.MessageType;
import com.apibotmessage.service.MessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MessageProcessorImpl implements MessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(MessageProcessorImpl.class);

    @Override
    @Async("webhookTaskExecutor")
    public void process(MetaWebhookEvent event) {
        if (event == null || event.getEntries() == null) {
            log.warn("Evento webhook vazio ou sem entries");
            return;
        }

        for (var entry : event.getEntries()) {
            if (entry.getChanges() == null) continue;

            for (var change : entry.getChanges()) {
                if (change.getValue() == null) continue;

                var value = change.getValue();
                var metadata = value.getMetadata();
                String phoneNumberId = metadata != null ? metadata.getPhoneNumberId() : "unknown";

                // Processa mensagens recebidas
                if (value.getMessages() != null) {
                    for (MetaMessage message : value.getMessages()) {
                        processIncomingMessage(message, phoneNumberId);
                    }
                }

                // Processa status de entrega (sent, delivered, read, failed)
                if (value.getStatuses() != null) {
                    for (var status : value.getStatuses()) {
                        log.info("Status update: messageId={} status={} recipientId={}",
                                status.getId(), status.getStatus(), status.getRecipientId());
                        // TODO: persistir status, notificar callback, etc.
                    }
                }
            }
        }
    }

    private void processIncomingMessage(MetaMessage message, String phoneNumberId) {
        String from = message.getFrom();
        String textContent = message.extractTextContent();
        MessageType type = message.getMessageType();

        log.info("Mensagem recebida: from={} type={} text={} phoneNumberId={}",
                maskPhone(from), type, truncate(textContent, 100), phoneNumberId);

        // TODO: Implementar lógica de negócio aqui
        // Ex: rotear para bot, salvar no banco, disparar webhook interno, etc.
        // handleBusinessLogic(from, textContent, type, phoneNumberId);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, 3) + "***" + phone.substring(phone.length() - 2);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }
}