package com.clientjourney.channel.telegram;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TelegramPollingAdapterSkeletonIntegrationTest {

    @Test
    void shouldMapMockProviderFailuresToNormalizedCodes() {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();

        Map<String, Object> tooManyRequests = adapter.normalizeProviderHttpFailure(
            429,
            "{\"description\":\"Too many requests\"}"
        );
        assertEquals("TEMPORARY_PROVIDER_ERROR", tooManyRequests.get("normalizedCode"));
        assertEquals("Too many requests", tooManyRequests.get("detail"));

        Map<String, Object> unavailable = adapter.normalizeProviderHttpFailure(
            503,
            "{\"error\":\"Provider unavailable\"}"
        );
        assertEquals("TEMPORARY_PROVIDER_ERROR", unavailable.get("normalizedCode"));
        assertEquals("Provider unavailable", unavailable.get("detail"));

        Map<String, Object> malformed = adapter.normalizeProviderHttpFailure(
            400,
            "{broken-json"
        );
        assertEquals("PERMANENT_PROVIDER_ERROR", malformed.get("normalizedCode"));
        assertEquals("malformed-json", malformed.get("detail"));
    }

    @Test
    void shouldNormalizeRealHttpProviderResponses() throws Exception {
        TelegramPollingAdapterSkeleton adapter = new TelegramPollingAdapterSkeleton();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/429", exchange -> {
            byte[] body = "{\"description\":\"Rate limit\"}".getBytes();
            exchange.sendResponseHeaders(429, body.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(body); }
        });
        server.createContext("/503", exchange -> {
            byte[] body = "{\"error\":\"Temporarily unavailable\"}".getBytes();
            exchange.sendResponseHeaders(503, body.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(body); }
        });
        server.createContext("/400", exchange -> {
            byte[] body = "{oops".getBytes();
            exchange.sendResponseHeaders(400, body.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(body); }
        });
        server.start();
        try {
            int port = server.getAddress().getPort();
            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> r429 = client.send(HttpRequest.newBuilder().uri(URI.create("http://localhost:" + port + "/429")).GET().build(), HttpResponse.BodyHandlers.ofString());
            Map<String, Object> n429 = adapter.normalizeProviderHttpFailure(r429.statusCode(), r429.body());
            assertEquals("TEMPORARY_PROVIDER_ERROR", n429.get("normalizedCode"));
            assertEquals("Rate limit", n429.get("detail"));

            HttpResponse<String> r503 = client.send(HttpRequest.newBuilder().uri(URI.create("http://localhost:" + port + "/503")).GET().build(), HttpResponse.BodyHandlers.ofString());
            Map<String, Object> n503 = adapter.normalizeProviderHttpFailure(r503.statusCode(), r503.body());
            assertEquals("TEMPORARY_PROVIDER_ERROR", n503.get("normalizedCode"));

            HttpResponse<String> r400 = client.send(HttpRequest.newBuilder().uri(URI.create("http://localhost:" + port + "/400")).GET().build(), HttpResponse.BodyHandlers.ofString());
            Map<String, Object> n400 = adapter.normalizeProviderHttpFailure(r400.statusCode(), r400.body());
            assertEquals("PERMANENT_PROVIDER_ERROR", n400.get("normalizedCode"));
            assertEquals("malformed-json", n400.get("detail"));
        } finally {
            server.stop(0);
        }
    }
}
