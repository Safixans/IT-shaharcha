package com.itshaharcha.portfolio.security;

import java.util.EnumSet;
import java.util.Set;

/**
 * Maps platform roles to the per-resource {@link Permission}s they hold.
 *
 * <p>Portfolio resources (certificates, education, items, the portfolio itself) are
 * owner-scoped: any authenticated account manages its own, so those endpoints need
 * only authentication. The single elevated action is certificate verification, held
 * by reviewers (TEACHER) and admins.
 */
public final class RolePermissions {

    private static final Set<Permission> REVIEWER = EnumSet.of(Permission.CERTIFICATE_VERIFY);

    private RolePermissions() {
    }

    /** Resolve the permissions granted by a single role authority (e.g. {@code ROLE_TEACHER}). */
    public static Set<Permission> forRole(String role) {
        if (role == null) {
            return Set.of();
        }
        return switch (role) {
            case "ROLE_ADMIN", "ROLE_TEACHER" -> REVIEWER;
            default -> Set.of();
        };
    }
}
