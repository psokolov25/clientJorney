package com.clientjourney.channel.telegram;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TelegramPollingAdapterSkeletonTest {
    @Test
    void shouldExposeAdapterMetadata() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();
        assertEquals("TELEGRAM_POLLING", adapter.adapterType());
        assertEquals("NOT_IMPLEMENTED", adapter.status());
    }
}
