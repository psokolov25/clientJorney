package com.clientjourney.channel.whatsapp;

import com.clientjourney.channel.spi.ClientInputMessage;
import com.clientjourney.channel.spi.ClientOutputMessage;
import com.clientjourney.channel.spi.ClientOutputMessageType;

import java.util.Map;

public class WhatsAppChannelAdapterSkeleton {
    public ClientOutputMessage handle(ClientInputMessage input) {
        return new ClientOutputMessage(ClientOutputMessageType.INFO, "SKELETON", "WhatsApp skeleton adapter", Map.of("channel", "whatsapp"));
    }
}
