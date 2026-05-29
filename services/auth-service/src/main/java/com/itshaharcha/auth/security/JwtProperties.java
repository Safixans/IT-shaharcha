package com.itshaharcha.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * JWT signing configuration. The initial build uses HS256 with a shared secret
 * for simplicity; for production, switch to RS256 with an asymmetric keypair and
 * distribute the public key (JWKS) to the gateway and downstream services.
 */
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String secret,
        String issuer,
        Duration accessTokenTtl,
        Duration refreshTokenTtl) {

    public JwtProperties {
        if (issuer == null || issuer.isBlank()) {
            issuer = "it-shaharcha-auth";
        }
        if (accessTokenTtl == null) {
            accessTokenTtl = Duration.ofMinutes(15);
        }
        if (refreshTokenTtl == null) {
            refreshTokenTtl = Duration.ofDays(7);
        }
    }
}
