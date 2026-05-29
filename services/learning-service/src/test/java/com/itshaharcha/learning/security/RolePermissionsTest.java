package com.itshaharcha.learning.security;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RolePermissionsTest {

    @Test
    void student_holdsAllReadButNoWriteEditOrDelete() {
        Set<Permission> perms = RolePermissions.forRole("ROLE_STUDENT");
        assertThat(perms).contains(Permission.COURSE_READ, Permission.TRACK_READ, Permission.SOURCE_READ);
        assertThat(perms).noneMatch(p -> p.name().endsWith("_WRITE")
                || p.name().endsWith("_EDIT") || p.name().endsWith("_DELETE"));
    }

    @Test
    void teacher_holdsReadWriteEditButNotDelete() {
        Set<Permission> perms = RolePermissions.forRole("ROLE_TEACHER");
        assertThat(perms).contains(Permission.COURSE_READ, Permission.COURSE_WRITE, Permission.COURSE_EDIT);
        assertThat(perms).noneMatch(p -> p.name().endsWith("_DELETE"));
    }

    @Test
    void admin_holdsEveryPermissionIncludingDelete() {
        Set<Permission> perms = RolePermissions.forRole("ROLE_ADMIN");
        assertThat(perms).containsExactlyInAnyOrder(Permission.values());
    }

    @Test
    void unknownOrNullRole_holdsNothing() {
        assertThat(RolePermissions.forRole("ROLE_GUEST")).isEmpty();
        assertThat(RolePermissions.forRole(null)).isEmpty();
    }
}
