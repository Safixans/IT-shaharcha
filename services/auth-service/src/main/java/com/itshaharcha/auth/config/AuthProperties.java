package com.itshaharcha.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Auth behavior toggles.
 *
 * @param autoActivate when true, new accounts are created already ACTIVE and email-verified,
 *                      skipping OTP verification. Intended for local/dev only — keep false in
 *                      production so the email verification flow is enforced.
 */
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(boolean autoActivate) {
}
