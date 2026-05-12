package com.clientjourney.app;

import com.clientjourney.app.security.SecurityPolicyConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemSecurityControllerTest {
    @Test
    void policy_mask_ratelimit_should_return_baseline_data() {
        SecurityPolicyConfig cfg = new SecurityPolicyConfig();
        cfg.setApiKeyEnabled(true);
        cfg.setRateLimitPerMinute(77);
        cfg.setPiiMaskingEnabled(true);
        cfg.setOidcEnabled(true);
        cfg.setOidcIssuer("https://keycloak.local/realms/client-journey");
        cfg.setAccessBoundaryMode("OIDC_REQUIRED_FOR_ADMIN");

        SystemSecurityController c = new SystemSecurityController(cfg);
        assertEquals("enabled", c.policy().get("apiKeyAuth"));
        assertEquals("enabled", c.policy().get("oidc"));
        assertEquals("https://keycloak.local/realms/client-journey", c.policy().get("oidcIssuer"));
        assertEquals("OIDC_REQUIRED_FOR_ADMIN", c.policy().get("accessBoundaryMode"));
        assertEquals(77, c.policy().get("rateLimitPerMinute"));
        assertEquals("12***90", c.mask("1234567890").get("masked"));
        assertEquals(77, c.rateLimit().get("maxRequests"));

        var batch = c.maskBatch(java.util.List.of("user@example.com", "1234567890"));
        assertEquals(2, batch.get("count"));
    }
}
