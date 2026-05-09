package com.clientjourney.channel.spi;

import java.util.Map;

public record ClientOutputMessage(
    ClientOutputMessageType type,
    String nodeId,
    String text,
    Map<String, Object> payload
) {
}
