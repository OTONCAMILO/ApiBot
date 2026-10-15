package com.apibotmessage.domain.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaStatus {
    private String id;
    private String status;
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("recipient_id")
    private String recipientId;
    @JsonProperty("conversation_id")
    private String conversationId;
    @JsonProperty("biz_opaque_callback_data")
    private String bizOpaqueCallbackData;
}