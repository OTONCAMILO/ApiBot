package com.apibotmessage.service.impl;

import com.apibotmessage.client.WhatsAppCloudClient;
import com.apibotmessage.domain.dto.outbound.SendMessageResponse;
import com.apibotmessage.service.MessageService;
import com.apibotmessage.util.BrazilianPhoneFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final WhatsAppCloudClient whatsAppCloudClient;
    private final BrazilianPhoneFormatter phoneFormatter;

    public MessageServiceImpl(WhatsAppCloudClient whatsAppCloudClient,
                              BrazilianPhoneFormatter phoneFormatter) {
        this.whatsAppCloudClient = whatsAppCloudClient;
        this.phoneFormatter = phoneFormatter;
    }

    @Override
    public Optional<SendMessageResponse> sendText(String to, String text) {
        String formattedTo = phoneFormatter.toE164(to);
        log.debug("Enviando texto para: {} (formatado: {})", maskPhone(to), maskPhone(formattedTo));
        return whatsAppCloudClient.sendText(formattedTo, text);
    }

    @Override
    public Optional<SendMessageResponse> sendTemplate(String to, String templateName,
                                                       String languageCode, Object components) {
        String formattedTo = phoneFormatter.toE164(to);
        return whatsAppCloudClient.sendTemplate(formattedTo, templateName, languageCode, components);
    }

    @Override
    public Optional<SendMessageResponse> sendMedia(String to, String type,
                                                    String mediaIdOrUrl, String caption, String filename) {
        String formattedTo = phoneFormatter.toE164(to);
        return whatsAppCloudClient.sendMedia(formattedTo, type, mediaIdOrUrl, caption, filename);
    }

    @Override
    public Optional<String> uploadMedia(byte[] fileBytes, String mimeType, String filename) {
        return whatsAppCloudClient.uploadMedia(fileBytes, mimeType, filename);
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, 3) + "***" + phone.substring(phone.length() - 2);
    }
}