package com.apibotmessage.client;

import com.apibotmessage.client.dto.MediaUploadResponse;
import com.apibotmessage.domain.dto.outbound.SendMessageRequest;
import com.apibotmessage.domain.dto.outbound.SendMessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

@Component
public class WhatsAppCloudClient {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppCloudClient.class);

    private final RestClient restClient;
    private final String apiVersion;
    private final String defaultPhoneNumberId;
    private final String defaultAccessToken;

    public WhatsAppCloudClient(RestClient restClient,
                               @Value("${whatsapp.api.version:v21.0}") String apiVersion,
                               @Value("${whatsapp.phone-number-id:}") String defaultPhoneNumberId,
                               @Value("${whatsapp.access-token:}") String defaultAccessToken) {
        this.restClient = restClient;
        this.apiVersion = apiVersion;
        this.defaultPhoneNumberId = defaultPhoneNumberId;
        this.defaultAccessToken = defaultAccessToken;
    }

    /**
     * Envia mensagem de texto simples.
     * Retorna Optional com response (id da mensagem) ou empty se falhou.
     */
    public Optional<SendMessageResponse> sendText(String to, String text) {
        return sendText(to, text, defaultPhoneNumberId, defaultAccessToken);
    }

    /**
     * Envia mensagem de texto com phoneNumberId e token específicos (multi-tenant).
     */
    public Optional<SendMessageResponse> sendText(String to, String text,
                                                   String phoneNumberId, String accessToken) {
        var request = new SendMessageRequest();
        request.setMessagingProduct("whatsapp");
        request.setTo(to);
        request.setType("text");
        var textContent = new SendMessageRequest.TextContent();
        textContent.setBody(text);
        request.setText(textContent);
        return executeSend(request, phoneNumberId, accessToken);
    }

    /**
     * Envia template (HSM - Highly Structured Message).
     */
    public Optional<SendMessageResponse> sendTemplate(String to, String templateName,
                                                       String languageCode, Object components) {
        return sendTemplate(to, templateName, languageCode, components,
                defaultPhoneNumberId, defaultAccessToken);
    }

    public Optional<SendMessageResponse> sendTemplate(String to, String templateName,
                                                       String languageCode, Object components,
                                                       String phoneNumberId, String accessToken) {
        var request = new SendMessageRequest();
        request.setMessagingProduct("whatsapp");
        request.setTo(to);
        request.setType("template");

        var templateContent = new SendMessageRequest.TemplateContent();
        templateContent.setName(templateName);
        var lang = new SendMessageRequest.TemplateContent.Language();
        lang.setCode(languageCode);
        lang.setPolicy("deterministic");
        templateContent.setLanguage(lang);
        templateContent.setComponents(components);
        request.setTemplate(templateContent);

        return executeSend(request, phoneNumberId, accessToken);
    }

    /**
     * Envia mídia (imagem, documento, áudio, vídeo) por link ou media_id.
     */
    public Optional<SendMessageResponse> sendMedia(String to, String type, String mediaIdOrUrl,
                                                    String caption, String filename) {
        return sendMedia(to, type, mediaIdOrUrl, caption, filename,
                defaultPhoneNumberId, defaultAccessToken);
    }

    public Optional<SendMessageResponse> sendMedia(String to, String type, String mediaIdOrUrl,
                                                    String caption, String filename,
                                                    String phoneNumberId, String accessToken) {
        var request = new SendMessageRequest();
        request.setMessagingProduct("whatsapp");
        request.setTo(to);
        request.setType(type);

        var media = new SendMessageRequest.MediaContent();
        if (mediaIdOrUrl.startsWith("http")) {
            media.setLink(mediaIdOrUrl);
        } else {
            media.setId(mediaIdOrUrl);
        }
        media.setCaption(caption);
        media.setFilename(filename);

        switch (type) {
            case "image" -> request.setImage(media);
            case "document" -> request.setDocument(media);
            case "audio" -> request.setAudio(media);
            case "video" -> request.setVideo(media);
            default -> throw new IllegalArgumentException("Tipo de mídia não suportado: " + type);
        }

        return executeSend(request, phoneNumberId, accessToken);
    }

    /**
     * Upload de mídia para obter media_id (para reutilização).
     * Meta exige multipart/form-data.
     */
    public Optional<String> uploadMedia(byte[] fileBytes, String mimeType, String filename) {
        return uploadMedia(fileBytes, mimeType, filename, defaultPhoneNumberId, defaultAccessToken);
    }

    public Optional<String> uploadMedia(byte[] fileBytes, String mimeType, String filename,
                                         String phoneNumberId, String accessToken) {
        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException("Arquivo vazio");
        }

        String url = String.format("/%s/%s/media", apiVersion, phoneNumberId);

        try {
            // Usa MultiValueMap para multipart
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("messaging_product", "whatsapp");
            parts.add("type", mimeType.split("/")[0]);

            // File part
            var filePart = new org.springframework.http.HttpEntity<byte[]>(fileBytes, createFileHeaders(mimeType, filename));
            parts.add("file", filePart);

            var response = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(parts)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw new RestClientException("Upload falhou: " + res.getStatusCode());
                    })
                    .body(MediaUploadResponse.class);

            log.info("Mídia enviada com sucesso: mediaId={}", response.getId());
            return Optional.ofNullable(response).map(MediaUploadResponse::getId);

        } catch (RestClientException e) {
            log.error("Erro no upload de mídia: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private HttpHeaders createFileHeaders(String mimeType, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(mimeType));
        headers.setContentDispositionFormData("file", filename);
        return headers;
    }

    /**
     * Execução genérica de envio com tratamento de erros da Graph API.
     */
    private Optional<SendMessageResponse> executeSend(SendMessageRequest request,
                                                       String phoneNumberId, String accessToken) {
        if (phoneNumberId == null || phoneNumberId.isBlank()) {
            log.error("phoneNumberId não configurado");
            return Optional.empty();
        }
        if (accessToken == null || accessToken.isBlank()) {
            log.error("accessToken não configurado");
            return Optional.empty();
        }

        String url = String.format("/%s/%s/messages", apiVersion, phoneNumberId);

        try {
            var response = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        String errorBody;
                        try {
                            errorBody = new String(res.getBody().readAllBytes());
                        } catch (Exception e) {
                            errorBody = "Unable to read error body";
                        }
                        log.error("Erro 4xx da Graph API: body={}", errorBody);
                        throw new RestClientException("Graph API 4xx: " + res.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        log.error("Erro 5xx da Graph API: {}", res.getStatusCode());
                        throw new RestClientException("Graph API 5xx: " + res.getStatusCode());
                    })
                    .body(SendMessageResponse.class);

            log.info("Mensagem enviada: to={} messageId={}", maskPhone(request.getTo()),
                    response.getMessages() != null && !response.getMessages().isEmpty()
                            ? response.getMessages().get(0).getId() : "unknown");
            return Optional.ofNullable(response);

        } catch (RestClientException e) {
            log.error("Falha ao enviar mensagem: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, 3) + "***" + phone.substring(phone.length() - 2);
    }
}