package com.itshaharcha.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitConfigTest {

    private final KeyResolver resolver = new RateLimitConfig().rateLimitKeyResolver();

    @Test
    void usesAccountId_whenHeaderPresent() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/x").header("X-Account-Id", "acct-1"));
        assertThat(resolver.resolve(exchange).block()).isEqualTo("acct-1");
    }

    @Test
    void fallsBackToClientIp_whenNoAccountId() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/x")
                        .remoteAddress(new InetSocketAddress("10.0.0.7", 12345)));
        assertThat(resolver.resolve(exchange).block()).isEqualTo("10.0.0.7");
    }

    @Test
    void returnsUnknown_whenNoAccountIdAndNoRemoteAddress() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/x"));
        assertThat(resolver.resolve(exchange).block()).isEqualTo("unknown");
    }

    @Test
    void blankAccountId_fallsBackToIp() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/x")
                        .header("X-Account-Id", "  ")
                        .remoteAddress(new InetSocketAddress("10.0.0.8", 80)));
        assertThat(resolver.resolve(exchange).block()).isEqualTo("10.0.0.8");
    }
}
