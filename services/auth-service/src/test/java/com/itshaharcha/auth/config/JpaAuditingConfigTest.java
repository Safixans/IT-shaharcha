package com.itshaharcha.auth.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JpaAuditingConfigTest {

    private final AuditorAware<String> auditor = new JpaAuditingConfig().auditorAware();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void systemAuditor_whenNoAuthentication() {
        SecurityContextHolder.clearContext();
        assertThat(auditor.getCurrentAuditor()).contains("system");
    }

    @Test
    void systemAuditor_whenNotAuthenticated() {
        var token = new TestingAuthenticationToken("alice", "pw");
        token.setAuthenticated(false);
        SecurityContextHolder.getContext().setAuthentication(token);
        assertThat(auditor.getCurrentAuditor()).contains("system");
    }

    @Test
    void principalAuditor_whenAuthenticated() {
        var token = new UsernamePasswordAuthenticationToken(
                "account-123", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(token);
        assertThat(auditor.getCurrentAuditor()).contains("account-123");
    }
}
