package com.clientjourney.channel.kafka;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KafkaChannelAdapterSkeletonTest {
    @Test
    void shouldExposeAdapterMetadata() {
        KafkaChannelAdapterSkeleton adapter = new KafkaChannelAdapterSkeleton();
        assertEquals("KAFKA", adapter.adapterType());
        assertEquals("NOT_IMPLEMENTED", adapter.status());
    }
}
