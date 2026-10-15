package com.apibotmessage.domain.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaMedia {
    private String id;
    private String mimeType;
    @JsonProperty("sha256")
    private String sha256;
    private String caption;
    @JsonProperty("filename")
    private String filename;
}