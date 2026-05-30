package com.itshaharcha.analytics.security;

import com.itshaharcha.common.exception.ApplicationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Reads the authenticated actor (set by {@link JwtAuthenticationFilter}) off the security context. */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static UUID currentAccountId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UUID accountId)) {
            throw ApplicationException.unauthorized("Authentication required");
        }
        return accountId;
    }

    public static Set<String> currentRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return Set.of();
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .collect(Collectors.toSet());
    }

    public static boolean isAdmin() {
        return currentRoles().contains("ROLE_ADMIN");
    }
}
