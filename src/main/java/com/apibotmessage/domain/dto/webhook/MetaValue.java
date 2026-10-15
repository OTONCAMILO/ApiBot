package com.apibotmessage.domain.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaValue {
    @JsonProperty("messaging_product")
    private String messagingProduct;

    @JsonProperty("metadata")
    private MetaMetadata metadata;

    @JsonProperty("contacts")
    private List<MetaContact> contacts;

    @JsonProperty("messages")
    private List<MetaMessage> messages;

    private List<MetaStatus> statuses;
}