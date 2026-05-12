package com.clientjourney.app.security;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("clientjourney.security")
public class SecurityPolicyConfig {
    private boolean apiKeyEnabled = false;
    private int rateLimitPerMinute = 120;
    private boolean piiMaskingEnabled = true;
    private boolean oidcEnabled = false;
    private String oidcIssuer = "";
    private String accessBoundaryMode = "API_KEY_OR_OIDC";

    public boolean isApiKeyEnabled() { return apiKeyEnabled; }
    public void setApiKeyEnabled(boolean apiKeyEnabled) { this.apiKeyEnabled = apiKeyEnabled; }
    public int getRateLimitPerMinute() { return rateLimitPerMinute; }
    public void setRateLimitPerMinute(int rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }
    public boolean isPiiMaskingEnabled() { return piiMaskingEnabled; }
    public void setPiiMaskingEnabled(boolean piiMaskingEnabled) { this.piiMaskingEnabled = piiMaskingEnabled; }
    public boolean isOidcEnabled() { return oidcEnabled; }
    public void setOidcEnabled(boolean oidcEnabled) { this.oidcEnabled = oidcEnabled; }
    public String getOidcIssuer() { return oidcIssuer; }
    public void setOidcIssuer(String oidcIssuer) { this.oidcIssuer = oidcIssuer; }
    public String getAccessBoundaryMode() { return accessBoundaryMode; }
    public void setAccessBoundaryMode(String accessBoundaryMode) { this.accessBoundaryMode = accessBoundaryMode; }
}
