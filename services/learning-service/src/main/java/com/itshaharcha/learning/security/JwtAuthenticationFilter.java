package com.itshaharcha.learning.security;

import com.itshaharcha.common.exception.ApplicationException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Authenticates requests bearing a valid ACCESS JWT issued by identity-service.
 * Grants both the role authorities ({@code ROLE_*}) and the per-resource
 * {@link Permission} authorities those roles imply (see {@link RolePermissions}).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Claims claims = tokenProvider.parse(token);
                tokenProvider.requireType(claims, TokenType.ACCESS);
                authenticate(request, claims);
            } catch (ApplicationException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, Claims claims) {
        Set<String> roles = tokenProvider.roles(claims);
        Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
            RolePermissions.forRole(role).forEach(p ->
                    authorities.add(new SimpleGrantedAuthority(p.name())));
        }
        var authentication = new UsernamePasswordAuthenticationToken(
                tokenProvider.accountId(claims), null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
