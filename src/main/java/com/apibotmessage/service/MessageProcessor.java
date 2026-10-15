package com.apibotmessage.service;

import com.apibotmessage.domain.dto.webhook.MetaWebhookEvent;

public interface MessageProcessor {
    void process(MetaWebhookEvent event);
}