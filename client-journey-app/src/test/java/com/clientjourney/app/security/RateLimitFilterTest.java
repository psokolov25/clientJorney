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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitFilterTest {

    @Test
    void shouldReturnTooManyRequestsAfterLimitExceeded() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(1);
        ServerFilterChain chain = mockChain();

        MutableHttpResponse<?> first = first(filter.doFilter(HttpRequest.GET("/api/runtime/scenarios/x/sessions"), chain));
        MutableHttpResponse<?> second = first(filter.doFilter(HttpRequest.GET("/api/runtime/scenarios/x/sessions"), chain));

        assertEquals(HttpStatus.OK, first.getStatus());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, second.getStatus());
    }

    @Test
    void shouldUsePathSpecificLimitRules() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(10, Map.of("/api/runtime/scenarios", 1));
        ServerFilterChain chain = mockChain();

        MutableHttpResponse<?> firstScenario = first(filter.doFilter(HttpRequest.GET("/api/runtime/scenarios/x/sessions"), chain));
        MutableHttpResponse<?> secondScenario = first(filter.doFilter(HttpRequest.GET("/api/runtime/scenarios/x/sessions"), chain));
        MutableHttpResponse<?> otherEndpoint = first(filter.doFilter(HttpRequest.GET("/api/runtime/sessions/x/answers"), chain));

        assertEquals(HttpStatus.OK, firstScenario.getStatus());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, secondScenario.getStatus());
        assertEquals(HttpStatus.OK, otherEndpoint.getStatus());
    }


    @Test
    void shouldParseRulesAndIgnoreInvalidTokens() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(5, "/api/runtime/scenarios=1,invalid,/api/runtime/sessions=2,/api/runtime/bad=-1");
        ServerFilterChain chain = mockChain();

        MutableHttpResponse<?> s1 = first(filter.doFilter(HttpRequest.GET("/api/runtime/scenarios/x/sessions"), chain));
        MutableHttpResponse<?> s2 = first(filter.doFilter(HttpRequest.GET("/api/runtime/scenarios/x/sessions"), chain));

        MutableHttpResponse<?> a1 = first(filter.doFilter(HttpRequest.GET("/api/runtime/sessions/x/answers"), chain));
        MutableHttpResponse<?> a2 = first(filter.doFilter(HttpRequest.GET("/api/runtime/sessions/x/answers"), chain));
        MutableHttpResponse<?> a3 = first(filter.doFilter(HttpRequest.GET("/api/runtime/sessions/x/answers"), chain));

        assertEquals(HttpStatus.OK, s1.getStatus());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, s2.getStatus());
        assertEquals(HttpStatus.OK, a1.getStatus());
        assertEquals(HttpStatus.OK, a2.getStatus());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, a3.getStatus());
    }

    @Test
    void shouldKeepSeparateCountersForRuleAndDefaultWindows() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(1, Map.of("/api/runtime/scenarios", 2));
        ServerFilterChain chain = mockChain();

        MutableHttpResponse<?> default1 = first(filter.doFilter(HttpRequest.GET("/api/runtime/other"), chain));
        MutableHttpResponse<?> default2 = first(filter.doFilter(HttpRequest.GET("/api/runtime/other"), chain));

        MutableHttpResponse<?> scenario1 = first(filter.doFilter(HttpRequest.GET("/api/runtime/scenarios/x/sessions"), chain));
        MutableHttpResponse<?> scenario2 = first(filter.doFilter(HttpRequest.GET("/api/runtime/scenarios/x/sessions"), chain));
        MutableHttpResponse<?> scenario3 = first(filter.doFilter(HttpRequest.GET("/api/runtime/scenarios/x/sessions"), chain));

        assertEquals(HttpStatus.OK, default1.getStatus());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, default2.getStatus());
        assertEquals(HttpStatus.OK, scenario1.getStatus());
        assertEquals(HttpStatus.OK, scenario2.getStatus());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, scenario3.getStatus());
    }
    private static ServerFilterChain mockChain() {
        ServerFilterChain chain = Mockito.mock(ServerFilterChain.class);
        Mockito.when(chain.proceed(Mockito.any())).thenReturn(io.micronaut.core.async.publisher.Publishers.just(HttpResponse.ok()));
        return chain;
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


    @Test
    void shouldPreferFirstMatchingRuleForOverlappingPrefixes() throws Exception {
        java.util.Map<String,Integer> rules = new java.util.LinkedHashMap<>();
        rules.put("/api/runtime/scenarios", 1);
        rules.put("/api/runtime", 3);
        RateLimitFilter filter = new RateLimitFilter(10, rules);
        ServerFilterChain chain = mockChain();

        MutableHttpResponse<?> first = first(filter.doFilter(HttpRequest.GET("/api/runtime/scenarios/x/sessions"), chain));
        MutableHttpResponse<?> second = first(filter.doFilter(HttpRequest.GET("/api/runtime/scenarios/x/sessions"), chain));

        assertEquals(HttpStatus.OK, first.getStatus());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, second.getStatus());
    }
}
