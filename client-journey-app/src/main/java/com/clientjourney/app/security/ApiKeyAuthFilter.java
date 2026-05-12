package com.clientjourney.app.security;

import io.micronaut.context.annotation.Value;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;

@Filter("/api/admin/**")
public class ApiKeyAuthFilter implements HttpServerFilter {
    private final String apiKey;

    public ApiKeyAuthFilter(@Value("${clientjourney.security.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        if (apiKey == null || apiKey.isBlank()) {
            return chain.proceed(request);
        }
        String header = request.getHeaders().get("X-API-Key");
        if (!apiKey.equals(header)) {
            return io.micronaut.core.async.publisher.Publishers.just(HttpResponse.status(HttpStatus.UNAUTHORIZED));
        }
        return chain.proceed(request);
    }
}
