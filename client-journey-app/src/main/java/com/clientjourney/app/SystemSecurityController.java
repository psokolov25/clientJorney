package com.clientjourney.app;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.swagger.v3.oas.annotations.Operation;

import java.util.Map;
import com.clientjourney.app.security.PiiMaskingUtil;

@Controller("/api/system/security")
public class SystemSecurityController {
    private final com.clientjourney.app.security.SecurityPolicyConfig config;

    public SystemSecurityController(com.clientjourney.app.security.SecurityPolicyConfig config) {
        this.config = config;
    }

    @Operation(summary = "Политика безопасности", description = "Возвращает текущую политику безопасности: API Key/OIDC/rate-limit/masking.")
    @Get("/policy")
    public Map<String, Object> policy() {
        return Map.of(
            "apiKeyAuth", config.isApiKeyEnabled() ? "enabled" : "disabled",
            "oidc", config.isOidcEnabled() ? "enabled" : "disabled",
            "oidcIssuer", config.getOidcIssuer(),
            "accessBoundaryMode", config.getAccessBoundaryMode(),
            "rateLimitPerMinute", config.getRateLimitPerMinute(),
            "piiMasking", config.isPiiMaskingEnabled() ? "enabled" : "disabled"
        );
    }

    @Operation(summary = "Предпросмотр маскирования PII", description = "Демонстрирует маскирование e-mail/телефона по текущим правилам.")
    @Get("/mask")
    public Map<String, Object> mask(@QueryValue String value) {
        String masked = value.contains("@") ? PiiMaskingUtil.maskEmail(value) : PiiMaskingUtil.maskPhone(value);
        return Map.of("originalLength", value.length(), "masked", masked);
    }

    @Operation(summary = "Маскирование списка значений", description = "Маскирует список PII-значений (email/phone) в одном запросе.")
    @Post("/mask-batch")
    public Map<String, Object> maskBatch(@Body java.util.List<String> values) {
        java.util.List<String> masked = values == null ? java.util.List.of() : values.stream()
            .map(v -> v != null && v.contains("@") ? PiiMaskingUtil.maskEmail(v) : PiiMaskingUtil.maskPhone(v == null ? "" : v))
            .toList();
        return Map.of("count", masked.size(), "masked", masked);
    }

    @Operation(summary = "Политика rate-limit", description = "Возвращает параметры rate-limit политики для операционного контроля.")
    @Get("/rate-limit")
    public Map<String, Object> rateLimit() {
        return Map.of("windowSeconds", 60, "maxRequests", config.getRateLimitPerMinute(), "mode", "enforced");
    }
}
