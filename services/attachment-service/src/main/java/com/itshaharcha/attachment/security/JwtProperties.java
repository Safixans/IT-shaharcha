package com.itshaharcha.attachment.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(String secret, String issuer) {

    public JwtProperties {
        if (issuer == null || issuer.isBlank()) {
            issuer = "it-shaharcha-auth";
        }
    }
}
