package com.apibotmessage.domain.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaContext {
    @JsonProperty("from")
    private String from;
    private String id;
    @JsonProperty("referred_product")
    private String referredProduct;
}