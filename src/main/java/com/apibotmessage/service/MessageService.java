package com.apibotmessage.service;

import com.apibotmessage.domain.dto.outbound.SendMessageResponse;

import java.util.Optional;

public interface MessageService {
    Optional<SendMessageResponse> sendText(String to, String text);
    Optional<SendMessageResponse> sendTemplate(String to, String templateName, String languageCode, Object components);
    Optional<SendMessageResponse> sendMedia(String to, String type, String mediaIdOrUrl, String caption, String filename);
    Optional<String> uploadMedia(byte[] fileBytes, String mimeType, String filename);
}