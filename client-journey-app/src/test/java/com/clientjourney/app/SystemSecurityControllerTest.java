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

        SystemSecurityController c = new SystemSecurityController(cfg);
        assertEquals("enabled", c.policy().get("apiKeyAuth"));
        assertEquals(77, c.policy().get("rateLimitPerMinute"));
        assertEquals("12***90", c.mask("1234567890").get("masked"));
    }
}
