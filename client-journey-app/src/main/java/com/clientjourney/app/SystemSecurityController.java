package com.clientjourney.app;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.QueryValue;

import java.util.Map;
import com.clientjourney.app.security.PiiMaskingUtil;

@Controller("/api/system/security")
public class SystemSecurityController {
    private final com.clientjourney.app.security.SecurityPolicyConfig config;

    public SystemSecurityController(com.clientjourney.app.security.SecurityPolicyConfig config) {
        this.config = config;
    }

    @Get("/policy")
    public Map<String, Object> policy() {
        return Map.of(
            "apiKeyAuth", config.isApiKeyEnabled() ? "enabled" : "disabled",
            "rateLimitPerMinute", config.getRateLimitPerMinute(),
            "piiMasking", config.isPiiMaskingEnabled() ? "enabled" : "disabled"
        );
    }

    @Get("/mask")
    public Map<String, Object> mask(@QueryValue String value) {
        String masked = value.contains("@") ? PiiMaskingUtil.maskEmail(value) : PiiMaskingUtil.maskPhone(value);
        return Map.of("originalLength", value.length(), "masked", masked);
    }

    @Get("/rate-limit")
    public Map<String, Object> rateLimit() {
        return Map.of("windowSeconds", 60, "maxRequests", 120, "mode", "report-only");
    }
}
