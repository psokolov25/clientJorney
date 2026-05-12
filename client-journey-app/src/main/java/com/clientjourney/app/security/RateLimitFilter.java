package com.clientjourney.app.security;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Filter("/api/runtime/**")
public class RateLimitFilter implements HttpServerFilter {
    private final int defaultLimit;
    private final Map<String, Integer> pathLimits;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(
            @Value("${clientjourney.security.rate-limit-per-minute:120}") int defaultLimit,
            @Value("${clientjourney.security.rate-limit-rules:}") String rules
    ) {
        this(defaultLimit, parseRules(rules));
    }

    RateLimitFilter(int defaultLimit) {
        this(defaultLimit, Map.of());
    }

    RateLimitFilter(int defaultLimit, Map<String, Integer> pathLimits) {
        this.defaultLimit = defaultLimit;
        this.pathLimits = pathLimits == null ? Map.of() : pathLimits;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        String path = request.getPath();
        int effectiveLimit = resolveLimit(path);
        String remoteKey = request.getRemoteAddress() == null ? "unknown" : request.getRemoteAddress().toString();
        String key = remoteKey + "|" + matchedPrefix(path);

        Window window = windows.computeIfAbsent(key, k -> new Window());
        synchronized (window) {
            long now = Instant.now().getEpochSecond();
            if (now - window.epochSecond >= 60) {
                window.epochSecond = now;
                window.counter.set(0);
            }
            if (window.counter.incrementAndGet() > effectiveLimit) {
                return io.micronaut.core.async.publisher.Publishers.just(HttpResponse.status(HttpStatus.TOO_MANY_REQUESTS));
            }
        }
        return chain.proceed(request);
    }

    private int resolveLimit(String path) {
        for (Map.Entry<String, Integer> entry : pathLimits.entrySet()) {
            if (path.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return defaultLimit;
    }

    private String matchedPrefix(String path) {
        for (String prefix : pathLimits.keySet()) {
            if (path.startsWith(prefix)) {
                return prefix;
            }
        }
        return "default";
    }

    private static Map<String, Integer> parseRules(String rules) {
        if (rules == null || rules.isBlank()) {
            return Map.of();
        }
        Map<String, Integer> parsed = new LinkedHashMap<>();
        for (String token : rules.split(",")) {
            String trimmed = token.trim();
            if (trimmed.isBlank() || !trimmed.contains("=")) {
                continue;
            }
            String[] parts = trimmed.split("=", 2);
            String prefix = parts[0].trim();
            try {
                int value = Integer.parseInt(parts[1].trim());
                if (!prefix.isEmpty() && value > 0) {
                    parsed.put(prefix, value);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return parsed;
    }

    private static final class Window {
        long epochSecond = Instant.now().getEpochSecond();
        AtomicInteger counter = new AtomicInteger();
    }
}
