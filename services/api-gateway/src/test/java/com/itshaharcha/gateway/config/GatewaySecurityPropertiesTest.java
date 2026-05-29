package com.itshaharcha.gateway.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GatewaySecurityPropertiesTest {

    @Test
    void appliesDefaultIssuerAndOpenPatterns_whenNull() {
        var props = new GatewaySecurityProperties("secret", null, null);
        assertThat(props.jwtIssuer()).isEqualTo("it-shaharcha-auth");
        assertThat(props.openPatterns())
                .contains("/api/v1/auth/login", "/api/v1/auth/register", "/actuator/health/**");
    }

    @Test
    void appliesDefaultOpenPatterns_whenEmpty() {
        var props = new GatewaySecurityProperties("secret", "iss", List.of());
        assertThat(props.openPatterns()).isNotEmpty();
        assertThat(props.jwtIssuer()).isEqualTo("iss");
    }

    @Test
    void blankIssuer_fallsBackToDefault() {
        var props = new GatewaySecurityProperties("secret", "   ", List.of("/x"));
        assertThat(props.jwtIssuer()).isEqualTo("it-shaharcha-auth");
    }

    @Test
    void retainsCustomValues() {
        var props = new GatewaySecurityProperties("secret", "custom-iss", List.of("/public/**"));
        assertThat(props.jwtIssuer()).isEqualTo("custom-iss");
        assertThat(props.openPatterns()).containsExactly("/public/**");
    }
}
