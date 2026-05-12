package com.clientjourney.app.security;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Filter("/api/runtime/**")
public class RateLimitFilter implements HttpServerFilter {
    private final int limit;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public RateLimitFilter(@Value("${clientjourney.security.rate-limit-per-minute:120}") int limit) {
        this.limit = limit;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        String key = request.getRemoteAddress().map(Object::toString).orElse("unknown");
        Window window = windows.computeIfAbsent(key, k -> new Window());
        synchronized (window) {
            long now = Instant.now().getEpochSecond();
            if (now - window.epochSecond >= 60) {
                window.epochSecond = now;
                window.counter.set(0);
            }
            if (window.counter.incrementAndGet() > limit) {
                return io.micronaut.core.async.publisher.Publishers.just(HttpResponse.status(HttpStatus.TOO_MANY_REQUESTS));
            }
        }
        return chain.proceed(request);
    }

    private static final class Window {
        long epochSecond = Instant.now().getEpochSecond();
        AtomicInteger counter = new AtomicInteger();
    }
}
