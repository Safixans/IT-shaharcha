package com.itshaharcha.portfolio.security;

/**
 * Per-resource permissions granted to a request as {@code GrantedAuthority}s based
 * on the caller's roles (see {@link RolePermissions}). Most portfolio resources are
 * owner-scoped (any authenticated user manages their own), so the only elevated
 * permission is verifying someone else's certificate — held by reviewers (TEACHER)
 * and admins. Used by {@code @PreAuthorize("hasAuthority('CERTIFICATE_VERIFY')")}.
 */
public enum Permission {
    CERTIFICATE_VERIFY
}
