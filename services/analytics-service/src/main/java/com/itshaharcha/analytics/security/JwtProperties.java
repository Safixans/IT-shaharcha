package com.itshaharcha.analytics.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT validation configuration. Analytics does NOT mint tokens; it validates the
 * HS256 access tokens issued by identity-service. Secret + issuer MUST match the
 * values identity signs with.
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(String secret, String issuer) {

    public JwtProperties {
        if (issuer == null || issuer.isBlank()) {
            issuer = "it-shaharcha-auth";
        }
    }
}
