package com.clientjourney.channel.spi;

import java.util.Map;

/**
 * Результат попытки доставки сообщения во внешний канал.
 */
public record DeliveryResult(
        boolean delivered,
        int attempts,
        String providerMessageId,
        String errorCode,
        String errorMessage,
        Map<String, Object> debug
) {
    public static DeliveryResult success(int attempts, String providerMessageId, Map<String, Object> debug) {
        return new DeliveryResult(true, attempts, providerMessageId, null, null, debug);
    }

    public static DeliveryResult failure(int attempts, String errorCode, String errorMessage, Map<String, Object> debug) {
        return new DeliveryResult(false, attempts, null, errorCode, errorMessage, debug);
    }
}
