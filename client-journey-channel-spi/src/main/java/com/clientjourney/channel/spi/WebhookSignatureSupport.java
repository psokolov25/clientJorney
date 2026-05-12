package com.clientjourney.channel.spi;

/**
 * Контракт для адаптеров, которые умеют проверять подпись входящих webhook-событий.
 */
public interface WebhookSignatureSupport {
    boolean validateWebhookSignature(String payload, String signature, String secret);
}
