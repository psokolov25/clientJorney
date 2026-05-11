package com.clientjourney.channel.telegram;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

@Requires(property = "client-journey.channel.telegram.enabled", value = "true")
@Singleton
public class TelegramPollingAdapterSkeleton {

    public String adapterType() {
        return "TELEGRAM_POLLING";
    }

    public String status() {
        return "NOT_IMPLEMENTED";
    }
}
