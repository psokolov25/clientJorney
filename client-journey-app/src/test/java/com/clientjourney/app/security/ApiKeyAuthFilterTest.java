package com.clientjourney.app.security;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.filter.ServerFilterChain;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiKeyAuthFilterTest {

    @Test
    void shouldRejectWhenApiKeyDoesNotMatch() throws Exception {
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter("secret");
        ServerFilterChain chain = Mockito.mock(ServerFilterChain.class);

        MutableHttpResponse<?> response = first(filter.doFilter(HttpRequest.GET("/api/admin/scenarios"), chain));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatus());
        Mockito.verify(chain, Mockito.never()).proceed(Mockito.any());
    }

    @Test
    void shouldAllowWhenApiKeyMatches() throws Exception {
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter("secret");
        ServerFilterChain chain = Mockito.mock(ServerFilterChain.class);
        Mockito.when(chain.proceed(Mockito.any())).thenReturn(io.micronaut.core.async.publisher.Publishers.just(HttpResponse.ok()));

        MutableHttpResponse<?> response = first(filter.doFilter(HttpRequest.GET("/api/admin/scenarios").header("X-API-Key", "secret"), chain));
        assertEquals(HttpStatus.OK, response.getStatus());
        Mockito.verify(chain).proceed(Mockito.any());
    }

    private static MutableHttpResponse<?> first(Publisher<MutableHttpResponse<?>> publisher) throws Exception {
        CompletableFuture<MutableHttpResponse<?>> future = new CompletableFuture<>();
        publisher.subscribe(new Subscriber<>() {
            @Override public void onSubscribe(Subscription s) { s.request(1); }
            @Override public void onNext(MutableHttpResponse<?> response) { future.complete(response); }
            @Override public void onError(Throwable t) { future.completeExceptionally(t); }
            @Override public void onComplete() { }
        });
        return future.get(3, TimeUnit.SECONDS);
    }
}
