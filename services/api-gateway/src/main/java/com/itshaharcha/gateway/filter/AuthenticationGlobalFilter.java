package com.itshaharcha.gateway.filter;

import com.itshaharcha.gateway.config.GatewaySecurityProperties;
import com.itshaharcha.gateway.security.JwtVerifier;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Validates the bearer access token for protected routes and forwards trusted
 * identity headers (X-Account-Id, X-Username, X-Roles) downstream. Open paths
 * (login, register, docs, health) bypass validation.
 */
@Slf4j
@Component
public class AuthenticationGlobalFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String HDR_ACCOUNT_ID = "X-Account-Id";
    private static final String HDR_USERNAME = "X-Username";
    private static final String HDR_ROLES = "X-Roles";

    private final JwtVerifier jwtVerifier;
    private final List<String> openPatterns;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthenticationGlobalFilter(JwtVerifier jwtVerifier,
                                      GatewaySecurityProperties properties) {
        this.jwtVerifier = jwtVerifier;
        this.openPatterns = properties.openPatterns();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isOpen(path)) {
            // Strip any client-supplied identity headers on open routes to prevent spoofing.
            return chain.filter(stripIdentityHeaders(exchange));
        }

        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return unauthorized(exchange, "Missing or malformed Authorization header");
        }

        try {
            Claims claims = jwtVerifier.verifyAccessToken(header.substring(BEARER_PREFIX.length()));
            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .headers(h -> {
                        h.remove(HDR_ACCOUNT_ID);
                        h.remove(HDR_USERNAME);
                        h.remove(HDR_ROLES);
                        h.set(HDR_ACCOUNT_ID, claims.getSubject());
                        h.set(HDR_USERNAME, jwtVerifier.username(claims));
                        h.set(HDR_ROLES, jwtVerifier.roles(claims));
                    })
                    .build();
            return chain.filter(exchange.mutate().request(mutated).build());
        } catch (Exception ex) {
            log.debug("JWT validation failed for {}: {}", path, ex.getMessage());
            return unauthorized(exchange, "Invalid or expired token");
        }
    }

    private boolean isOpen(String path) {
        return openPatterns.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    private ServerWebExchange stripIdentityHeaders(ServerWebExchange exchange) {
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .headers(h -> {
                    h.remove(HDR_ACCOUNT_ID);
                    h.remove(HDR_USERNAME);
                    h.remove(HDR_ROLES);
                })
                .build();
        return exchange.mutate().request(mutated).build();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"success":false,"code":"UNAUTHORIZED","message":"%s"}"""
                .formatted(message);
        var buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        // Run early, before routing.
        return -100;
    }
}
