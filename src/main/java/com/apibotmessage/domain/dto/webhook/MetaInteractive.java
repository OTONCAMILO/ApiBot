package com.apibotmessage.domain.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaInteractive {
    private String type;
    @JsonProperty("button_reply")
    private MetaButtonReply buttonReply;
    @JsonProperty("list_reply")
    private MetaListReply listReply;
}