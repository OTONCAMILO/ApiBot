package com.apibotmessage.domain.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaEntry {
    private String id;
    @JsonProperty("changes")
    private List<MetaChange> changes;
}