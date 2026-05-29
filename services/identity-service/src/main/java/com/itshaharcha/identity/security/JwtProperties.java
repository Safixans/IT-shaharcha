package com.itshaharcha.identity.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * JWT signing configuration. HS256 with a shared secret; the gateway must be
 * configured with the same secret + issuer so it can validate the tokens this
 * service mints and forward identity headers downstream.
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
