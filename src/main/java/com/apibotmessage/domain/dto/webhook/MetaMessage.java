package com.apibotmessage.domain.dto.webhook;

import com.apibotmessage.domain.enums.MessageType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaMessage {
    @JsonProperty("from")
    private String from;

    private String id;
    private String timestamp;

    @JsonProperty("type")
    private String typeRaw;

    @JsonProperty("text")
    private MetaText text;

    @JsonProperty("image")
    private MetaMedia image;

    @JsonProperty("audio")
    private MetaMedia audio;

    @JsonProperty("video")
    private MetaMedia video;

    @JsonProperty("document")
    private MetaMedia document;

    @JsonProperty("location")
    private MetaLocation location;

    @JsonProperty("contacts")
    private List<MetaContact> contacts;

    @JsonProperty("interactive")
    private MetaInteractive interactive;

    @JsonProperty("button")
    private MetaButton button;

    @JsonProperty("reaction")
    private MetaReaction reaction;

    @JsonProperty("sticker")
    private MetaMedia sticker;

    @JsonProperty("system")
    private MetaSystem system;

    @JsonProperty("context")
    private MetaContext context;

    @JsonIgnore
    public MessageType getMessageType() {
        try {
            return MessageType.valueOf(typeRaw.toUpperCase());
        } catch (Exception e) {
            return MessageType.UNKNOWN;
        }
    }

    @JsonIgnore
    public String extractTextContent() {
        if (text != null && text.getBody() != null) return text.getBody();
        if (interactive != null) {
            if (interactive.getButtonReply() != null) return interactive.getButtonReply().getTitle();
            if (interactive.getListReply() != null) return interactive.getListReply().getTitle();
        }
        if (button != null && button.getText() != null) return button.getText();
        if (reaction != null && reaction.getEmoji() != null) return "[reação: " + reaction.getEmoji() + "]";
        if (system != null && system.getBody() != null) return "[sistema: " + system.getBody() + "]";
        return "[mensagem não textual: " + typeRaw + "]";
    }
}