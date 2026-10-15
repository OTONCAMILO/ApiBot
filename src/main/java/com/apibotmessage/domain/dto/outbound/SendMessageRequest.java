package com.apibotmessage.domain.dto.outbound;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SendMessageRequest {
    @JsonProperty("messaging_product")
    private String messagingProduct = "whatsapp";

    @JsonProperty("to")
    private String to;

    @JsonProperty("type")
    private String type;

    @JsonProperty("text")
    private TextContent text;

    @JsonProperty("image")
    private MediaContent image;

    @JsonProperty("document")
    private MediaContent document;

    @JsonProperty("audio")
    private MediaContent audio;

    @JsonProperty("video")
    private MediaContent video;

    @JsonProperty("location")
    private LocationContent location;

    @JsonProperty("contacts")
    private List<ContactContent> contacts;

    @JsonProperty("interactive")
    private InteractiveContent interactive;

    @JsonProperty("template")
    private TemplateContent template;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TextContent {
        private String body;
        private boolean previewUrl = false;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MediaContent {
        private String id;
        private String link;
        private String caption;
        @JsonProperty("filename")
        private String filename;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LocationContent {
        private double latitude;
        private double longitude;
        private String name;
        private String address;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContactContent {
        private ContactAddress address;
        private ContactName name;
        private List<ContactPhone> phones;
        private List<ContactEmail> emails;
        private List<ContactUrl> urls;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactAddress {
        private String street, city, state, zip, country, countryCode, type;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactName {
        private String formattedName, firstName, lastName, middleName, suffix, prefix;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactPhone {
        private String phone, type, waId;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactEmail {
        private String email, type;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactUrl {
        private String url, type;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class InteractiveContent {
        private String type;
        private InteractiveHeader header;
        private InteractiveBody body;
        private InteractiveFooter footer;
        private InteractiveAction action;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InteractiveHeader {
        private String type;
        private TextContent text;
        private MediaContent image;
        private MediaContent document;
        private MediaContent video;
        private LocationContent location;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InteractiveBody {
        private String text;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InteractiveFooter {
        private String text;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InteractiveAction {
        private String button;
        private List<InteractiveButton> buttons;
        private InteractiveSection section;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InteractiveButton {
        private String type;
        private TextContent reply;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InteractiveSection {
        private String title;
        private List<InteractiveRow> rows;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InteractiveRow {
        private String id;
        private String title;
        private String description;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TemplateContent {
        private String name;
        private Language language;
        private Object components;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Language {
            private String code;
            private String policy = "deterministic";
        }
    }
}