package com.itshaharcha.gateway.security;

import com.itshaharcha.gateway.config.GatewaySecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtVerifierTest {

    private static final String SECRET = "test-secret-that-is-definitely-long-enough-32+";
    private static final String ISSUER = "it-shaharcha-auth";

    private final JwtVerifier verifier =
            new JwtVerifier(new GatewaySecurityProperties(SECRET, ISSUER, null));

    private static SecretKey key(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private String token(String issuer, String type, Object roles, String username, String secret) {
        var builder = Jwts.builder()
                .issuer(issuer)
                .subject("acct-1")
                .claim("type", type)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key(secret));
        if (roles != null) builder.claim("roles", roles);
        if (username != null) builder.claim("username", username);
        return builder.compact();
    }

    @Test
    void verifyAccessToken_returnsClaims_forValidAccessToken() {
        String t = token(ISSUER, "ACCESS", List.of("ROLE_STUDENT"), "jane", SECRET);
        Claims claims = verifier.verifyAccessToken(t);
        assertThat(claims.getSubject()).isEqualTo("acct-1");
        assertThat(verifier.username(claims)).isEqualTo("jane");
        assertThat(verifier.roles(claims)).isEqualTo("ROLE_STUDENT");
    }

    @Test
    void roles_joinsMultipleWithComma() {
        String t = token(ISSUER, "ACCESS", List.of("ROLE_STUDENT", "ROLE_TEACHER"), "jane", SECRET);
        Claims claims = verifier.verifyAccessToken(t);
        assertThat(verifier.roles(claims)).contains("ROLE_STUDENT", "ROLE_TEACHER");
        assertThat(verifier.roles(claims)).contains(",");
    }

    @Test
    void roles_emptyWhenClaimAbsent() {
        String t = token(ISSUER, "ACCESS", null, "jane", SECRET);
        Claims claims = verifier.verifyAccessToken(t);
        assertThat(verifier.roles(claims)).isEmpty();
    }

    @Test
    void verifyAccessToken_rejectsRefreshToken() {
        String t = token(ISSUER, "REFRESH", null, null, SECRET);
        assertThatThrownBy(() -> verifier.verifyAccessToken(t))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void verifyAccessToken_rejectsWrongIssuer() {
        String t = token("someone-else", "ACCESS", List.of("ROLE_STUDENT"), "jane", SECRET);
        assertThatThrownBy(() -> verifier.verifyAccessToken(t))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void verifyAccessToken_rejectsWrongSignature() {
        String t = token(ISSUER, "ACCESS", List.of("ROLE_STUDENT"), "jane",
                "another-secret-that-is-also-long-enough-32+!!");
        assertThatThrownBy(() -> verifier.verifyAccessToken(t))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void constructor_rejectsShortSecret() {
        assertThatThrownBy(() -> new JwtVerifier(
                new GatewaySecurityProperties("short", ISSUER, null)))
                .isInstanceOf(IllegalStateException.class);
    }
}
