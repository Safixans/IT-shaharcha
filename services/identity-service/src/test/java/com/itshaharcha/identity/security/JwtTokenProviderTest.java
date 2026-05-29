package com.itshaharcha.identity.security;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.exception.ErrorCode;
import com.itshaharcha.identity.entity.Account;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET = "integration-test-secret-key-that-is-long-enough-32+";

    private JwtTokenProvider provider(Duration accessTtl, Duration refreshTtl) {
        return new JwtTokenProvider(new JwtProperties(SECRET, "it-shaharcha-auth", accessTtl, refreshTtl));
    }

    private JwtTokenProvider provider() {
        return provider(Duration.ofMinutes(15), Duration.ofDays(7));
    }

    private Account account(UUID id, String username) {
        Account account = new Account();
        account.setId(id);
        account.setUsername(username);
        return account;
    }

    @Test
    void accessToken_roundTripsSubjectUsernameRolesAndType() {
        JwtTokenProvider provider = provider();
        UUID id = UUID.randomUUID();
        Account account = account(id, "jane");

        String token = provider.generateAccessToken(account, Set.of("ROLE_ADMIN", "ROLE_STUDENT"));
        Claims claims = provider.parse(token);

        provider.requireType(claims, TokenType.ACCESS);
        assertThat(provider.accountId(claims)).isEqualTo(id);
        assertThat(provider.username(claims)).isEqualTo("jane");
        assertThat(provider.roles(claims)).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_STUDENT");
    }

    @Test
    void refreshToken_roundTripsSubjectAndType() {
        JwtTokenProvider provider = provider();
        UUID id = UUID.randomUUID();

        String token = provider.generateRefreshToken(account(id, "jane"));
        Claims claims = provider.parse(token);

        provider.requireType(claims, TokenType.REFRESH);
        assertThat(provider.accountId(claims)).isEqualTo(id);
    }

    @Test
    void requireType_throwsWhenTypeMismatches() {
        JwtTokenProvider provider = provider();
        Claims claims = provider.parse(provider.generateRefreshToken(account(UUID.randomUUID(), "jane")));

        assertThatThrownBy(() -> provider.requireType(claims, TokenType.ACCESS))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TOKEN_INVALID);
    }

    @Test
    void parse_invalidToken_throwsTokenInvalid() {
        JwtTokenProvider provider = provider();

        assertThatThrownBy(() -> provider.parse("not-a-jwt"))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TOKEN_INVALID);
    }

    @Test
    void parse_tokenSignedByDifferentSecret_throwsTokenInvalid() {
        JwtTokenProvider issuer = new JwtTokenProvider(
                new JwtProperties("a-totally-different-secret-key-also-32-bytes!!", null, null, null));
        String foreign = issuer.generateAccessToken(account(UUID.randomUUID(), "jane"), Set.of());

        assertThatThrownBy(() -> provider().parse(foreign))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TOKEN_INVALID);
    }

    @Test
    void parse_expiredToken_throwsTokenExpired() {
        JwtTokenProvider provider = provider(Duration.ofSeconds(-1), Duration.ofDays(7));
        String token = provider.generateAccessToken(account(UUID.randomUUID(), "jane"), Set.of("ROLE_STUDENT"));

        assertThatThrownBy(() -> provider.parse(token))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TOKEN_EXPIRED);
    }

    @Test
    void roles_defaultsToEmptyWhenAbsent() {
        JwtTokenProvider provider = provider();
        Claims refreshClaims = provider.parse(provider.generateRefreshToken(account(UUID.randomUUID(), "jane")));

        assertThat(provider.roles(refreshClaims)).isEmpty();
    }

    @Test
    void accessTokenTtlSeconds_reflectsConfiguredDuration() {
        assertThat(provider(Duration.ofMinutes(15), Duration.ofDays(7)).accessTokenTtlSeconds())
                .isEqualTo(900L);
    }

    @Test
    void constructor_rejectsShortSecret() {
        assertThatThrownBy(() ->
                new JwtTokenProvider(new JwtProperties("too-short", null, null, null)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void properties_applyDefaultsForNullFields() {
        JwtProperties props = new JwtProperties(SECRET, null, null, null);

        assertThat(props.issuer()).isEqualTo("it-shaharcha-auth");
        assertThat(props.accessTokenTtl()).isEqualTo(Duration.ofMinutes(15));
        assertThat(props.refreshTokenTtl()).isEqualTo(Duration.ofDays(7));
    }
}
