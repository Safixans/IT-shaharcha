package com.itshaharcha.auth.security;

import com.itshaharcha.auth.entity.Account;
import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;
    private Account account;

    @BeforeEach
    void setUp() {
        var props = new JwtProperties(
                "test-secret-that-is-definitely-long-enough-32+",
                "it-shaharcha-auth",
                Duration.ofMinutes(15),
                Duration.ofDays(7));
        provider = new JwtTokenProvider(props);

        account = new Account();
        account.setId(UUID.randomUUID());
        account.setUsername("jane");
    }

    @Test
    void accessToken_carriesSubjectRolesAndType() {
        String token = provider.generateAccessToken(account, Set.of("ROLE_STUDENT"));
        Claims claims = provider.parse(token);

        provider.requireType(claims, TokenType.ACCESS);
        assertThat(provider.accountId(claims)).isEqualTo(account.getId());
        assertThat(provider.username(claims)).isEqualTo("jane");
        assertThat(provider.roles(claims)).containsExactly("ROLE_STUDENT");
    }

    @Test
    void requireType_rejectsMismatchedTokenType() {
        String refresh = provider.generateRefreshToken(account);
        Claims claims = provider.parse(refresh);

        assertThatThrownBy(() -> provider.requireType(claims, TokenType.ACCESS))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TOKEN_INVALID);
    }

    @Test
    void parse_rejectsTamperedToken() {
        String token = provider.generateAccessToken(account, Set.of("ROLE_STUDENT"));
        assertThatThrownBy(() -> provider.parse(token + "x"))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    void constructor_rejectsShortSecret() {
        var props = new JwtProperties("too-short", "iss",
                Duration.ofMinutes(1), Duration.ofMinutes(1));
        assertThatThrownBy(() -> new JwtTokenProvider(props))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void parse_rejectsExpiredToken_withExpiredCode() {
        var props = new JwtProperties(
                "test-secret-that-is-definitely-long-enough-32+",
                "it-shaharcha-auth",
                Duration.ofMillis(-1), // already expired on issue
                Duration.ofDays(7));
        var shortLived = new JwtTokenProvider(props);
        String token = shortLived.generateAccessToken(account, Set.of("ROLE_STUDENT"));

        assertThatThrownBy(() -> shortLived.parse(token))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TOKEN_EXPIRED);
    }

    @Test
    void parse_rejectsTokenFromDifferentIssuer() {
        var otherIssuer = new JwtTokenProvider(new JwtProperties(
                "test-secret-that-is-definitely-long-enough-32+",
                "someone-else",
                Duration.ofMinutes(15),
                Duration.ofDays(7)));
        String token = otherIssuer.generateAccessToken(account, Set.of("ROLE_STUDENT"));

        assertThatThrownBy(() -> provider.parse(token))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TOKEN_INVALID);
    }

    @Test
    void refreshToken_hasNoRolesClaim() {
        String refresh = provider.generateRefreshToken(account);
        Claims claims = provider.parse(refresh);
        assertThat(provider.roles(claims)).isEmpty();
    }

    @Test
    void accessTokenTtlSeconds_reflectsConfiguredDuration() {
        assertThat(provider.accessTokenTtlSeconds()).isEqualTo(Duration.ofMinutes(15).toSeconds());
    }

    @Test
    void refreshTokenExpiry_isInTheFuture() {
        assertThat(provider.refreshTokenExpiry()).isAfter(java.time.Instant.now());
    }
}
