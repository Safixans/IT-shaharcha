package com.itshaharcha.auth.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesTest {

    @Test
    void appliesDefaults_whenNull() {
        var props = new JwtProperties("secret", null, null, null);
        assertThat(props.issuer()).isEqualTo("it-shaharcha-auth");
        assertThat(props.accessTokenTtl()).isEqualTo(Duration.ofMinutes(15));
        assertThat(props.refreshTokenTtl()).isEqualTo(Duration.ofDays(7));
    }

    @Test
    void blankIssuer_fallsBackToDefault() {
        var props = new JwtProperties("secret", "  ", Duration.ofMinutes(5), Duration.ofHours(1));
        assertThat(props.issuer()).isEqualTo("it-shaharcha-auth");
    }

    @Test
    void retainsExplicitValues() {
        var props = new JwtProperties("secret", "custom",
                Duration.ofMinutes(5), Duration.ofHours(1));
        assertThat(props.issuer()).isEqualTo("custom");
        assertThat(props.accessTokenTtl()).isEqualTo(Duration.ofMinutes(5));
        assertThat(props.refreshTokenTtl()).isEqualTo(Duration.ofHours(1));
    }
}
