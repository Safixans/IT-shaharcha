package com.itshaharcha.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Edge security config. {@code openPatterns} are ant-style paths that bypass JWT
 * validation (login, register, docs, health). Everything else requires a valid
 * ACCESS token, which the gateway verifies before forwarding identity headers.
 */
@ConfigurationProperties(prefix = "app.gateway.security")
public record GatewaySecurityProperties(
        String jwtSecret,
        String jwtIssuer,
        List<String> openPatterns) {

    public GatewaySecurityProperties {
        if (jwtIssuer == null || jwtIssuer.isBlank()) {
            jwtIssuer = "it-shaharcha-auth";
        }
        if (openPatterns == null || openPatterns.isEmpty()) {
            openPatterns = List.of(
                    "/api/v1/auth/login",
                    "/api/v1/auth/register",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/verify",
                    "/api/v1/auth/otp:resend",
                    // Public learning catalog reads (spec marks these security: []).
                    // Learning re-validates tokens on its colon-verb POST actions.
                    "/api/v1/learning/tracks",
                    "/api/v1/learning/courses",
                    "/api/v1/learning/courses/*",
                    "/api/v1/learning/tutorials",
                    "/api/v1/learning/docs",
                    "/api/v1/learning/typing/lessons",
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/*/v3/api-docs/**",
                    "/*/swagger-ui/**",
                    "/actuator/health/**");
        }
    }
}
