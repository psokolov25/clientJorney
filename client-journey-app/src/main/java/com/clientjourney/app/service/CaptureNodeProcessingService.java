package com.clientjourney.app.service;

import com.clientjourney.domain.model.NodeType;
import com.clientjourney.domain.model.ScenarioNode;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import io.micronaut.json.JsonMapper;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import jakarta.inject.Singleton;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Singleton
public class CaptureNodeProcessingService {
    private final HttpClient httpClient;
    private final JsonMapper jsonMapper = JsonMapper.createDefault();

    public CaptureNodeProcessingService(@Client("/") HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Map<String, Object> process(ScenarioNode node, String answerValue, Map<String, Object> metadata) {
        Map<String, Object> safeMetadata = metadata == null ? Map.of() : metadata;
        String traceId = UUID.randomUUID().toString();
        if (node.type() == NodeType.API_CAPTURE) {
            return withEnvelope(node.id(), "API_CAPTURE", traceId, callApi(node, answerValue, safeMetadata));
        }
        if (node.type() == NodeType.GROOVY_CAPTURE) {
            return withEnvelope(node.id(), "GROOVY_CAPTURE", traceId, runGroovy(node, answerValue, safeMetadata));
        }
        return Map.of();
    }

    private Map<String, Object> withEnvelope(String nodeId, String captureType, String traceId, Map<String, Object> payload) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("capture.lastNodeId", nodeId);
        out.put("capture.lastType", captureType);
        out.put("capture.lastTraceId", traceId);
        out.put("capture.lastOutput", payload);
        out.put("capture.node." + nodeId, payload);
        return out;
    }

    private Map<String, Object> callApi(ScenarioNode node, String answerValue, Map<String, Object> metadata) {
        String url = node.code();
        String method = "POST";
        String payloadField = "answerValue";
        String nextInputField = null;
        if (node.code() != null && node.code().trim().startsWith("{")) {
            try {
                Map<String, Object> config = jsonMapper.readValue(node.code(), Map.class);
                url = String.valueOf(config.getOrDefault("url", ""));
                method = String.valueOf(config.getOrDefault("method", "POST"));
                payloadField = String.valueOf(config.getOrDefault("payloadField", "answerValue"));
                Object nextField = config.get("nextInputField");
                nextInputField = nextField == null ? null : String.valueOf(nextField);
            } catch (Exception ex) {
                return Map.of("capture.api.error", "Invalid API_CAPTURE config JSON: " + ex.getMessage());
            }
        }
        if (url == null || url.isBlank()) {
            return Map.of("capture.error", "API_CAPTURE node has empty code/url");
        }

        method = method == null ? "POST" : method.trim().toUpperCase(Locale.ROOT);
        if (!"GET".equals(method) && !"POST".equals(method)) {
            return Map.of("capture.api.error", "Unsupported method: " + method + ". Allowed: GET, POST");
        }
        if (!isAllowedHttpUrl(url)) {
            return Map.of("capture.api.error", "Unsupported url: only absolute http(s) URLs are allowed");
        }
        if (httpClient == null) {
            return Map.of("capture.api.request", answerValue, "capture.api.error", "HttpClient is not configured");
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put(payloadField, answerValue);
        Object captureInput = metadata.containsKey("capture.nextInput") ? metadata.get("capture.nextInput") : metadata.get("captureNextInput");
        payload.put("captureInput", captureInput);
        payload.put("metadata", metadata);
        try {
            String response;
            if ("GET".equalsIgnoreCase(method)) {
                response = httpClient.toBlocking().retrieve(HttpRequest.GET(url));
            } else {
                response = httpClient.toBlocking().retrieve(HttpRequest.POST(url, payload));
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("capture.api.request", answerValue);
            result.put("capture.api.response", response);
            if (nextInputField != null && !nextInputField.isBlank()) {
                try {
                    Map<String, Object> responseMap = jsonMapper.readValue(response, Map.class);
                    if (responseMap.containsKey(nextInputField)) {
                        Object nextInput = responseMap.get(nextInputField);
                        result.put("__nextInput", nextInput);
                        result.put("nextInput", nextInput);
                    }
                } catch (Exception ignored) {
                    // response may be non-json
                }
            }
            return result;
        } catch (Exception ex) {
            return Map.of("capture.api.request", answerValue, "capture.api.error", ex.getMessage());
        }
    }


    private boolean isAllowedHttpUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(url.trim());
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) && uri.getHost() != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    private Map<String, Object> runGroovy(ScenarioNode node, String answerValue, Map<String, Object> metadata) {
        String script = node.code();
        if (script == null || script.isBlank()) {
            return Map.of("capture.error", "GROOVY_CAPTURE node has empty code/script");
        }
        try {
            Binding binding = new Binding();
            binding.setVariable("answerValue", answerValue);
            binding.setVariable("metadata", metadata);
            binding.setVariable("previousCapture", metadata.get("capture.lastOutput"));
            Object result = new GroovyShell(binding).evaluate(script);
            if (result instanceof Map<?, ?> mapResult) {
                Map<String, Object> normalized = new LinkedHashMap<>();
                normalized.put("capture.groovy.request", answerValue);
                mapResult.forEach((k, v) -> normalized.put(String.valueOf(k), v));
                if (mapResult.containsKey("__nextInput") && !mapResult.containsKey("nextInput")) {
                    normalized.put("nextInput", mapResult.get("__nextInput"));
                }
                return normalized;
            }
            return Map.of("capture.groovy.request", answerValue, "capture.groovy.result", result == null ? "null" : String.valueOf(result));
        } catch (Exception ex) {
            return Map.of("capture.groovy.request", answerValue, "capture.groovy.error", ex.getMessage());
        }
    }
}
