package com.clientjourney.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SystemSecurityControllerTest {
    @Test
    void policy_mask_ratelimit_should_return_baseline_data() {
        SystemSecurityController c = new SystemSecurityController();
        assertEquals("baseline", c.policy().get("apiKeyAuth"));
        assertEquals("report-only", c.rateLimit().get("mode"));
        assertEquals("12***90", c.mask("1234567890").get("masked"));
    }
}
