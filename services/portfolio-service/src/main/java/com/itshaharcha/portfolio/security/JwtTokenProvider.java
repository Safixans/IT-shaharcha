package com.itshaharcha.portfolio.security;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Validates HS256 access tokens minted by identity-service. */
@Component
public class JwtTokenProvider {

    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_USERNAME = "username";

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "app.security.jwt.secret must be at least 32 bytes for HS256");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public Claims parse(String token) {
        try {
            return Jwts.parser()
                    .requireIssuer(properties.issuer())
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new ApplicationException(ErrorCode.TOKEN_EXPIRED, "Token has expired");
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ApplicationException(ErrorCode.TOKEN_INVALID, "Token is invalid");
        }
    }

    public void requireType(Claims claims, TokenType expected) {
        if (!expected.name().equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new ApplicationException(ErrorCode.TOKEN_INVALID, "Unexpected token type");
        }
    }

    public UUID accountId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public String username(Claims claims) {
        return claims.get(CLAIM_USERNAME, String.class);
    }

    @SuppressWarnings("unchecked")
    public Set<String> roles(Claims claims) {
        Object raw = claims.get(CLAIM_ROLES);
        if (raw instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.toSet());
        }
        return Set.of();
    }
}
