package com.itshaharcha.gateway.security;

import com.itshaharcha.gateway.config.GatewaySecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/** Verifies access-token signature/expiry and extracts identity claims. */
@Component
public class JwtVerifier {

    private final SecretKey key;
    private final String issuer;

    public JwtVerifier(GatewaySecurityProperties properties) {
        byte[] secret = properties.jwtSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException(
                    "app.gateway.security.jwt-secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(secret);
        this.issuer = properties.jwtIssuer();
    }

    /** Parses and validates the token, throwing if invalid/expired/not an ACCESS token. */
    public Claims verifyAccessToken(String token) {
        Claims claims = Jwts.parser()
                .requireIssuer(issuer)
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (!"ACCESS".equals(claims.get("type", String.class))) {
            throw new IllegalArgumentException("Not an access token");
        }
        return claims;
    }

    public String roles(Claims claims) {
        Object raw = claims.get("roles");
        if (raw instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.joining(","));
        }
        return "";
    }

    public String username(Claims claims) {
        return claims.get("username", String.class);
    }
}
