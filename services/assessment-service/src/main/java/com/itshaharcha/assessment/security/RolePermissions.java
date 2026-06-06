package com.itshaharcha.assessment.security;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * Maps platform roles to the per-resource {@link Permission}s they hold.
 *
 * <ul>
 *   <li>STUDENT — every {@code *_READ}.</li>
 *   <li>TEACHER — READ + WRITE + EDIT (author and edit exams, but not delete).</li>
 *   <li>ADMIN  — all permissions including DELETE.</li>
 * </ul>
 *
 * Tiers are cumulative by action suffix, so adding a new resource to
 * {@link Permission} automatically flows into every role.
 */
public final class RolePermissions {

    private static final Set<Permission> READ = byAction("_READ");
    private static final Set<Permission> WRITE = byAction("_WRITE");
    private static final Set<Permission> EDIT = byAction("_EDIT");
    private static final Set<Permission> DELETE = byAction("_DELETE");

    private static final Set<Permission> STUDENT = EnumSet.copyOf(READ);
    private static final Set<Permission> TEACHER = union(READ, WRITE, EDIT);
    private static final Set<Permission> ADMIN = union(READ, WRITE, EDIT, DELETE);

    private RolePermissions() {
    }

    /** Resolve the permissions granted by a single role authority (e.g. {@code ROLE_TEACHER}). */
    public static Set<Permission> forRole(String role) {
        if (role == null) {
            return Set.of();
        }
        return switch (role) {
            case "ROLE_ADMIN", "ROLE_MODERATOR" -> ADMIN;
            case "ROLE_TEACHER" -> TEACHER;
            case "ROLE_STUDENT" -> STUDENT;
            default -> Set.of();
        };
    }

    private static Set<Permission> byAction(String suffix) {
        return Arrays.stream(Permission.values())
                .filter(p -> p.name().endsWith(suffix))
                .collect(java.util.stream.Collectors.toCollection(() -> EnumSet.noneOf(Permission.class)));
    }

    @SafeVarargs
    private static Set<Permission> union(Set<Permission>... sets) {
        EnumSet<Permission> all = EnumSet.noneOf(Permission.class);
        for (Set<Permission> s : sets) {
            all.addAll(s);
        }
        return all;
    }
}
