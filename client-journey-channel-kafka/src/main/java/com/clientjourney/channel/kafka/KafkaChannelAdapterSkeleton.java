package com.clientjourney.channel.kafka;

import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

@Requires(property = "client-journey.channel.kafka.enabled", value = "true")
@Singleton
public class KafkaChannelAdapterSkeleton {

    public String adapterType() {
        return "KAFKA";
    }

    public String status() {
        return "NOT_IMPLEMENTED";
    }
}
