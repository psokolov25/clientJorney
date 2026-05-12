package com.clientjourney.app.security;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("clientjourney.security")
public class SecurityPolicyConfig {
    private boolean apiKeyEnabled = false;
    private int rateLimitPerMinute = 120;
    private boolean piiMaskingEnabled = true;

    public boolean isApiKeyEnabled() { return apiKeyEnabled; }
    public void setApiKeyEnabled(boolean apiKeyEnabled) { this.apiKeyEnabled = apiKeyEnabled; }
    public int getRateLimitPerMinute() { return rateLimitPerMinute; }
    public void setRateLimitPerMinute(int rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }
    public boolean isPiiMaskingEnabled() { return piiMaskingEnabled; }
    public void setPiiMaskingEnabled(boolean piiMaskingEnabled) { this.piiMaskingEnabled = piiMaskingEnabled; }
}
