package com.itshaharcha.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigBeansTest {

    private final SecurityConfig config = new SecurityConfig(null, new ObjectMapper());

    @Test
    void passwordEncoder_hashesAndMatches() {
        PasswordEncoder encoder = config.passwordEncoder();
        String hash = encoder.encode("Passw0rd");

        assertThat(hash).isNotEqualTo("Passw0rd");
        assertThat(encoder.matches("Passw0rd", hash)).isTrue();
        assertThat(encoder.matches("wrong", hash)).isFalse();
    }
}
