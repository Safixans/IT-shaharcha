package com.itshaharcha.gateway.filter;

import com.itshaharcha.gateway.config.GatewaySecurityProperties;
import com.itshaharcha.gateway.security.JwtVerifier;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationGlobalFilterTest {

    private static final String SECRET = "test-secret-that-is-definitely-long-enough-32+";
    private static final String ISSUER = "it-shaharcha-auth";

    private final GatewaySecurityProperties props =
            new GatewaySecurityProperties(SECRET, ISSUER, null);
    private final AuthenticationGlobalFilter filter =
            new AuthenticationGlobalFilter(new JwtVerifier(props), props);

    /** Captures the exchange handed to the downstream chain. */
    private static final class CapturingChain implements GatewayFilterChain {
        ServerWebExchange captured;
        @Override
        public Mono<Void> filter(ServerWebExchange exchange) {
            this.captured = exchange;
            return Mono.empty();
        }
    }

    private String accessToken() {
        return Jwts.builder()
                .issuer(ISSUER)
                .subject("acct-99")
                .claim("type", "ACCESS")
                .claim("username", "jane")
                .claim("roles", List.of("ROLE_STUDENT"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    @Test
    void orderIsEarly() {
        assertThat(filter.getOrder()).isEqualTo(-100);
    }

    @Test
    void openPath_bypassesAuth_andStripsSpoofedIdentityHeaders() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/v1/auth/login")
                        .header("X-Account-Id", "spoofed")
                        .header("X-Roles", "ROLE_ADMIN"));
        var chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        HttpHeaders forwarded = chain.captured.getRequest().getHeaders();
        assertThat(forwarded.getFirst("X-Account-Id")).isNull();
        assertThat(forwarded.getFirst("X-Roles")).isNull();
    }

    @Test
    void protectedPath_missingHeader_returns401() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/v1/profiles/me"));
        var chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.captured).isNull();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedPath_malformedHeader_returns401() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/profiles/me")
                        .header(HttpHeaders.AUTHORIZATION, "Basic abc"));
        var chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedPath_validToken_forwardsIdentityHeaders() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/profiles/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken()));
        var chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.captured).isNotNull();
        HttpHeaders forwarded = chain.captured.getRequest().getHeaders();
        assertThat(forwarded.getFirst("X-Account-Id")).isEqualTo("acct-99");
        assertThat(forwarded.getFirst("X-Username")).isEqualTo("jane");
        assertThat(forwarded.getFirst("X-Roles")).isEqualTo("ROLE_STUDENT");
    }

    @Test
    void protectedPath_invalidToken_returns401() {
        var exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/v1/profiles/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token"));
        var chain = new CapturingChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.captured).isNull();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
