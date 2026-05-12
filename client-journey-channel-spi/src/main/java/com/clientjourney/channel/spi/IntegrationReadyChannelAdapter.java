package com.clientjourney.channel.spi;

import java.util.List;

public interface IntegrationReadyChannelAdapter {
    List<String> requiredConfigKeys();
    boolean supportsWebhookSignatureValidation();

    /**
     * Базовый контракт исполнения исходящей отправки через внешний канал.
     */
    DeliveryResult executeOutbound(java.util.Map<String, Object> outboundPayload, int maxAttempts);
}
