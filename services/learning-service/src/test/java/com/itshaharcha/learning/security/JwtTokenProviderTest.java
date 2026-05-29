package com.itshaharcha.learning.security;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "integration-test-secret-key-that-is-long-enough-32+";
    private static final String ISSUER = "it-shaharcha-auth";

    private final JwtTokenProvider provider =
            new JwtTokenProvider(new JwtProperties(SECRET, ISSUER));

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private String mint(String issuer, String type, UUID sub, List<String> roles, Instant exp) {
        return Jwts.builder()
                .issuer(issuer)
                .subject(sub.toString())
                .claim("type", type)
                .claim("username", "alice")
                .claim("roles", roles)
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    @Test
    void shortSecret_throwsOnConstruction() {
        assertThatThrownBy(() -> new JwtTokenProvider(new JwtProperties("too-short", ISSUER)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void parse_validToken_extractsSubjectRolesAndType() {
        UUID sub = UUID.randomUUID();
        String token = mint(ISSUER, "ACCESS", sub, List.of("ROLE_STUDENT", "ROLE_TEACHER"),
                Instant.now().plusSeconds(600));

        Claims claims = provider.parse(token);

        assertThat(provider.accountId(claims)).isEqualTo(sub);
        assertThat(provider.username(claims)).isEqualTo("alice");
        assertThat(provider.roles(claims)).containsExactlyInAnyOrder("ROLE_STUDENT", "ROLE_TEACHER");
        provider.requireType(claims, TokenType.ACCESS);
    }

    @Test
    void requireType_mismatch_throwsTokenInvalid() {
        String token = mint(ISSUER, "REFRESH", UUID.randomUUID(), List.of(),
                Instant.now().plusSeconds(600));
        Claims claims = provider.parse(token);

        assertThatThrownBy(() -> provider.requireType(claims, TokenType.ACCESS))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TOKEN_INVALID);
    }

    @Test
    void parse_wrongIssuer_throwsTokenInvalid() {
        String token = mint("someone-else", "ACCESS", UUID.randomUUID(), List.of(),
                Instant.now().plusSeconds(600));

        assertThatThrownBy(() -> provider.parse(token))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TOKEN_INVALID);
    }

    @Test
    void parse_expiredToken_throwsTokenExpired() {
        String token = mint(ISSUER, "ACCESS", UUID.randomUUID(), List.of(),
                Instant.now().minusSeconds(60));

        assertThatThrownBy(() -> provider.parse(token))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TOKEN_EXPIRED);
    }
}
