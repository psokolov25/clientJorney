package com.clientjourney.app.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PiiMaskingUtilTest {
    @Test
    void should_mask_email_and_phone() {
        assertEquals("u***r@example.com", PiiMaskingUtil.maskEmail("user@example.com"));
        assertEquals("12***90", PiiMaskingUtil.maskPhone("1234567890"));
    }
}
