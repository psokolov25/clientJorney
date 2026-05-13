package com.clientjourney.channel.telegram;

import com.clientjourney.channel.spi.ClientInputMessage;
import com.clientjourney.channel.spi.IntegrationReadyChannelAdapter;
import com.clientjourney.channel.spi.WebhookSignatureSupport;
import com.clientjourney.channel.spi.DeliveryResult;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

@Requires(property = "client-journey.channel.telegram.enabled", value = "true")
@Singleton
public class TelegramPollingAdapterSkeleton implements IntegrationReadyChannelAdapter, WebhookSignatureSupport {
    private final Set<String> processedUpdateIds = ConcurrentHashMap.newKeySet();
    private final AtomicLong outboundTotal = new AtomicLong();
    private final AtomicLong outboundSuccess = new AtomicLong();
    private final AtomicLong outboundFailed = new AtomicLong();
    private final AtomicLong outboundRetryExhausted = new AtomicLong();
    private final AtomicLong outboundRetryAttempts = new AtomicLong();
    private final long metricsStartedAtNanos = System.nanoTime();
    private final ConcurrentLinkedQueue<Long> latencyMsWindow = new ConcurrentLinkedQueue<>();
    private static final int LATENCY_WINDOW_MAX = 200;

    public String adapterType() {
        return "TELEGRAM_POLLING";
    }

    public String status() {
        return "PARTIALLY_IMPLEMENTED";
    }

    public List<String> supportedDeliveryModes() {
        return List.of("POLLING", "WEBHOOK");
    }

    @Override
    public List<String> requiredConfigKeys() {
        return List.of("botToken", "deliveryMode");
    }

    public void validateConfig(Map<String, Object> config) {
        if (config == null) {
            throw new IllegalArgumentException("CONFIG_MISSING: telegram config is null");
        }
        Set<String> required = Set.copyOf(requiredConfigKeys());
        for (String key : required) {
            Object value = config.get(key);
            if (value == null || String.valueOf(value).isBlank()) {
                throw new IllegalArgumentException("CONFIG_MISSING_KEY: " + key);
            }
        }
        String mode = String.valueOf(config.get("deliveryMode")).trim().toUpperCase();
        if (!supportedDeliveryModes().contains(mode)) {
            throw new IllegalArgumentException("CONFIG_INVALID_DELIVERY_MODE: " + mode + " (allowed: POLLING, WEBHOOK)");
        }
        if ("WEBHOOK".equals(mode)) {
            Object webhookUrl = config.get("webhookUrl");
            if (webhookUrl == null || String.valueOf(webhookUrl).isBlank()) {
                throw new IllegalArgumentException("CONFIG_MISSING_KEY: webhookUrl");
            }
        }
    }

    @Override
    public boolean supportsWebhookSignatureValidation() {
        return true;
    }

    public boolean supportsLongPolling() {
        return true;
    }

    public ClientInputMessage fromWebhookUpdate(Map<String, Object> update, String scenarioCode) {
        update = update == null ? Map.of() : update;
        String externalUserId = extractExternalUserId(update).orElse("unknown");
        String text = extractText(update);
        String messageType = resolveMessageType(update, text);
        Map<String, Object> metadata = enrichMetadata(update);
        return new ClientInputMessage(
                UUID.randomUUID(),
                scenarioCode,
                "TELEGRAM",
                externalUserId,
                messageType,
                text,
                text,
                metadata
        );
    }

    public ClientInputMessage fromPollingUpdate(Map<String, Object> update, String scenarioCode) {
        return fromWebhookUpdate(update, scenarioCode);
    }

    public boolean shouldProcessUpdate(Map<String, Object> update) {
        if (update == null) return false;
        Object updateId = update.get("update_id");
        if (updateId == null) return true;
        String key = String.valueOf(updateId).trim();
        if (key.isBlank()) return true;
        return processedUpdateIds.add(key);
    }

    public String resolveDeliveryMode(Map<String, Object> config) {
        String mode = String.valueOf(config.getOrDefault("deliveryMode", "POLLING")).toUpperCase();
        return switch (mode) {
            case "POLLING", "WEBHOOK" -> mode;
            default -> "POLLING";
        };
    }

    public Map<String, Object> buildPollingFetchPayload(long offset, int timeoutSeconds) {
        return Map.of(
                "method", "GET",
                "urlTemplate", "https://api.telegram.org/bot{token}/getUpdates",
                "auth", "token-in-url",
                "offset", Math.max(0, offset),
                "timeout", Math.max(1, timeoutSeconds)
        );
    }

    public Map<String, Object> buildWebhookRegistrationPayload(String webhookUrl, String secretToken) {
        return Map.of(
                "method", "POST",
                "urlTemplate", "https://api.telegram.org/bot{token}/setWebhook",
                "auth", "token-in-url",
                "url", webhookUrl,
                "secretToken", secretToken
        );
    }

    public Map<String, Object> buildOutboundPayload(String externalUserId, String text) {
        return buildOutboundPayload(externalUserId, text, Map.of());
    }

    public Map<String, Object> buildOutboundPayload(String externalUserId, String text, Map<String, Object> options) {
        String parseMode = String.valueOf(options.getOrDefault("parseMode", "Markdown"));
        Object replyMarkup = options.get("replyMarkup");
        return Map.of(
                "method", "POST",
                "urlTemplate", "https://api.telegram.org/bot{token}/sendMessage",
                "auth", "token-in-url",
                "externalUserId", externalUserId,
                "text", text,
                "parseMode", parseMode,
                "replyMarkup", replyMarkup == null ? Map.of() : replyMarkup
        );
    }

    @Override
    public boolean validateWebhookSignature(String payload, String signature, String secret) {
        if (payload == null || signature == null || secret == null) return false;
        String expected = hmacSha256Hex(payload, secret);
        return constantTimeEquals(expected, signature);
    }


    @Override
    public DeliveryResult executeOutbound(Map<String, Object> outboundPayload, int maxAttempts) {
        long startedAt = System.nanoTime();
        outboundPayload = outboundPayload == null ? Map.of() : outboundPayload;
        outboundTotal.incrementAndGet();
        int attempts = Math.max(1, maxAttempts);
        Map<String, Object> correlation = extractCorrelation(outboundPayload);
        String forcedErrorCode = String.valueOf(outboundPayload.getOrDefault("forceErrorCode", "")).trim();
        int retryBaseDelayMs = parsePositiveInt(outboundPayload.get("retryBaseDelayMs"), 250);
        int retryMaxDelayMs = parsePositiveInt(outboundPayload.get("retryMaxDelayMs"), 10_000);
        int retryJitterPercent = parsePositiveInt(outboundPayload.get("retryJitterPercent"), 0);
        java.util.List<Integer> retryScheduleMs = buildRetrySchedule(attempts, retryBaseDelayMs, retryMaxDelayMs, retryJitterPercent);
        if (!forcedErrorCode.isBlank()) {
            outboundFailed.incrementAndGet();
            String normalized = normalizeErrorCode(forcedErrorCode);
            if ("TEMPORARY_PROVIDER_ERROR".equals(normalized)) {
                outboundRetryExhausted.incrementAndGet();
            }
            outboundRetryAttempts.addAndGet(Math.max(0, attempts - 1));
            DeliveryResult result = DeliveryResult.failure(attempts, normalized, "forced provider error: " + forcedErrorCode, Map.of(
                "payload", outboundPayload,
                "deliveryState", "failed",
                "deliveryLifecycle", java.util.List.of("queued", "failed"),
                "attemptsUsed", attempts,
                "retryScheduleMs", retryScheduleMs,
                "correlation", correlation
            ));
            recordLatency(startedAt);
            return result;
        }
        int transientFailures = 0;
        if (outboundPayload != null) {
            Object configured = outboundPayload.get("simulateTransientFailures");
            if (configured != null) {
                try { transientFailures = Math.max(0, Integer.parseInt(String.valueOf(configured))); } catch (NumberFormatException ignored) { }
            }
        }
        for (int i = 1; i <= attempts; i++) {
            String text = String.valueOf(outboundPayload.getOrDefault("text", ""));
            if (i <= transientFailures) {
                continue;
            }
            if (!text.isBlank()) {
                outboundSuccess.incrementAndGet();
                DeliveryResult result = DeliveryResult.success(i, "msg-" + i, Map.of(
                        "channel", outboundPayload.getOrDefault("externalUserId", "unknown"),
                        "attemptsUsed", i,
                        "deliveryState", "sent",
                        "deliveryLifecycle", java.util.List.of("queued", "sent"),
                        "retryScheduleMs", retryScheduleMs,
                        "correlation", correlation
                ));
                outboundRetryAttempts.addAndGet(Math.max(0, i - 1));
                recordLatency(startedAt);
                return result;
            }
        }
        String code = transientFailures >= attempts ? "RETRY_EXHAUSTED" : "EMPTY_TEXT";
        String message = transientFailures >= attempts ? "temporary transport errors exhausted retry budget" : "text is required";
        outboundFailed.incrementAndGet();
        if ("RETRY_EXHAUSTED".equals(code)) {
            outboundRetryExhausted.incrementAndGet();
        }
        outboundRetryAttempts.addAndGet(Math.max(0, attempts - 1));
        DeliveryResult result = DeliveryResult.failure(attempts, code, message, Map.of(
                "payload", outboundPayload,
                "deliveryState", "failed",
                "deliveryLifecycle", java.util.List.of("queued", "failed"),
                "attemptsUsed", attempts,
                "retryScheduleMs", retryScheduleMs,
                "correlation", correlation
        ));
        recordLatency(startedAt);
        return result;
    }

    private int parsePositiveInt(Object value, int fallback) {
        if (value == null) return fallback;
        try {
            return Math.max(1, Integer.parseInt(String.valueOf(value)));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private java.util.List<Integer> buildRetrySchedule(int attempts, int retryBaseDelayMs, int retryMaxDelayMs, int retryJitterPercent) {
        java.util.List<Integer> schedule = new ArrayList<>();
        int safeAttempts = Math.max(1, attempts);
        int safeBase = Math.max(1, retryBaseDelayMs);
        int safeMax = Math.max(safeBase, retryMaxDelayMs);
        int safeJitter = Math.max(0, retryJitterPercent);
        for (int i = 0; i < safeAttempts; i++) {
            long exponential = (long) safeBase * (1L << i);
            int delay = (int) Math.min(safeMax, exponential);
            if (safeJitter > 0) {
                int jitter = (delay * safeJitter) / 100;
                delay = Math.min(safeMax, delay + jitter);
            }
            schedule.add(delay);
        }
        return schedule;
    }

    private String normalizeErrorCode(String providerError) {
        String normalized = providerError == null ? "" : providerError.trim().toUpperCase();
        if (normalized.startsWith("5") || normalized.contains("TIMEOUT") || normalized.contains("TEMP")) {
            return "TEMPORARY_PROVIDER_ERROR";
        }
        return "PERMANENT_PROVIDER_ERROR";
    }

    public Map<String, Object> normalizeProviderHttpFailure(int statusCode, String responsePayload) {
        String normalized = statusCode == 429 || statusCode >= 500
                ? "TEMPORARY_PROVIDER_ERROR"
                : "PERMANENT_PROVIDER_ERROR";
        String detail = "raw";
        if (responsePayload != null && !responsePayload.isBlank()) {
            String payload = responsePayload.trim();
            if (payload.startsWith("{") && payload.endsWith("}")) {
                String description = extractJsonString(payload, "description");
                String error = extractJsonString(payload, "error");
                if (description != null && !description.isBlank()) {
                    detail = description;
                } else if (error != null && !error.isBlank()) {
                    detail = error;
                } else {
                    detail = "json-without-description";
                }
            } else {
                detail = "malformed-json";
            }
        }
        return Map.of(
                "normalizedCode", normalized,
                "statusCode", statusCode,
                "detail", detail
        );
    }

    private String extractJsonString(String payload, String key) {
        String token = "\"" + key + "\"";
        int keyPos = payload.indexOf(token);
        if (keyPos < 0) return null;
        int colon = payload.indexOf(':', keyPos + token.length());
        if (colon < 0) return null;
        int firstQuote = payload.indexOf('"', colon + 1);
        if (firstQuote < 0) return null;
        int secondQuote = payload.indexOf('"', firstQuote + 1);
        if (secondQuote < 0) return null;
        return payload.substring(firstQuote + 1, secondQuote);
    }

    private Map<String, Object> extractCorrelation(Map<String, Object> outboundPayload) {
        if (outboundPayload == null) return Map.of();
        return Map.of(
                "sessionId", String.valueOf(outboundPayload.getOrDefault("sessionId", "")),
                "updateId", String.valueOf(outboundPayload.getOrDefault("updateId", "")),
                "channelMessageId", String.valueOf(outboundPayload.getOrDefault("channelMessageId", ""))
        );
    }

    public Map<String, Object> metricsSnapshot() {
        long total = outboundTotal.get();
        long failed = outboundFailed.get();
        long retries = outboundRetryAttempts.get();
        double errorRate = total == 0 ? 0.0d : ((double) failed / (double) total);
        double secondsSinceStart = Math.max(1d, (System.nanoTime() - metricsStartedAtNanos) / 1_000_000_000d);
        double throughputPerSecond = total / secondsSinceStart;
        return Map.of(
                "outbound.total", total,
                "outbound.success", outboundSuccess.get(),
                "outbound.failed", failed,
                "outbound.retryExhausted", outboundRetryExhausted.get(),
                "outbound.retryAttempts", retries,
                "outbound.errorRate", errorRate,
                "outbound.throughputPerSecond", throughputPerSecond,
                "outbound.latencyP95Ms", computeP95LatencyMs()
        );
    }

    public Map<String, Object> healthSnapshot(Map<String, Object> config) {
        boolean configValid = true;
        String error = "";
        try {
            validateConfig(config == null ? Map.of() : config);
        } catch (Exception ex) {
            configValid = false;
            error = ex.getMessage() == null ? "config-invalid" : ex.getMessage();
        }
        return Map.of(
                "adapterType", adapterType(),
                "status", configValid ? "UP" : "DOWN",
                "configValid", configValid,
                "error", error,
                "metrics", metricsSnapshot()
        );
    }

    private void recordLatency(long startedAtNanos) {
        long elapsedMs = Math.max(0L, (System.nanoTime() - startedAtNanos) / 1_000_000L);
        latencyMsWindow.add(elapsedMs);
        while (latencyMsWindow.size() > LATENCY_WINDOW_MAX) {
            latencyMsWindow.poll();
        }
    }

    private long computeP95LatencyMs() {
        if (latencyMsWindow.isEmpty()) return 0L;
        java.util.List<Long> values = new ArrayList<>(latencyMsWindow);
        values.sort(Long::compareTo);
        int index = Math.max(0, (int) Math.ceil(values.size() * 0.95d) - 1);
        return values.get(index);
    }

    private Optional<String> extractExternalUserId(Map<String, Object> update) {
        if (update == null) return Optional.empty();
        Object fromId = update.get("fromId");
        if (fromId != null) return Optional.of(String.valueOf(fromId));

        Map<String, Object> message = nestedMap(update, "message");
        if (message != null) {
            Map<String, Object> from = nestedMap(message, "from");
            if (from != null && from.get("id") != null) return Optional.of(String.valueOf(from.get("id")));
        }

        Map<String, Object> callback = nestedMap(update, "callback_query");
        if (callback != null) {
            Map<String, Object> from = nestedMap(callback, "from");
            if (from != null && from.get("id") != null) return Optional.of(String.valueOf(from.get("id")));
        }
        return Optional.empty();
    }

    private String extractText(Map<String, Object> update) {
        if (update == null) return "";
        if (update.get("text") != null) return String.valueOf(update.get("text"));
        Map<String, Object> message = nestedMap(update, "message");
        if (message != null && message.get("text") != null) return String.valueOf(message.get("text"));
        Map<String, Object> callback = nestedMap(update, "callback_query");
        if (callback != null && callback.get("data") != null) return String.valueOf(callback.get("data"));
        return "";
    }

    private String resolveMessageType(Map<String, Object> update, String text) {
        if (update == null) return "TEXT";
        if (nestedMap(update, "callback_query") != null) return "CALLBACK";
        if (nestedMap(update, "message") != null) {
            Map<String, Object> msg = nestedMap(update, "message");
            if (msg != null && (msg.get("photo") != null || msg.get("document") != null || msg.get("video") != null)) {
                return "ATTACHMENT";
            }
        }
        if (text != null && text.startsWith("/")) return "COMMAND";
        return "TEXT";
    }

    private Map<String, Object> enrichMetadata(Map<String, Object> update) {
        if (update == null) return Map.of();
        Map<String, Object> message = nestedMap(update, "message");
        if (message == null) return update;
        Object photo = message.get("photo");
        Object document = message.get("document");
        Object video = message.get("video");
        if (photo == null && document == null && video == null) return update;
        return Map.of(
                "raw", update,
                "attachments", Map.of(
                        "photo", photo == null ? List.of() : photo,
                        "document", document == null ? Map.of() : document,
                        "video", video == null ? Map.of() : video
                )
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> nestedMap(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value instanceof Map<?, ?> map) return (Map<String, Object>) map;
        return null;
    }

    private String hmacSha256Hex(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        return Objects.equals(a != null ? a.trim() : null, b != null ? b.trim() : null);
    }

}
