package com.itshaharcha.learning.security;

/**
 * Per-resource catalog/authoring permissions, granted to a request as
 * {@code GrantedAuthority}s based on the caller's roles (see {@link RolePermissions}).
 * Naming: {@code <RESOURCE>_<ACTION>} with ACTION in {READ, WRITE, EDIT, DELETE}.
 * Used by {@code @PreAuthorize("hasAuthority('COURSE_WRITE')")} on admin endpoints.
 */
public enum Permission {
    TRACK_READ, TRACK_WRITE, TRACK_EDIT, TRACK_DELETE,
    COURSE_READ, COURSE_WRITE, COURSE_EDIT, COURSE_DELETE,
    MODULE_READ, MODULE_WRITE, MODULE_EDIT, MODULE_DELETE,
    LESSON_READ, LESSON_WRITE, LESSON_EDIT, LESSON_DELETE,
    TUTORIAL_READ, TUTORIAL_WRITE, TUTORIAL_EDIT, TUTORIAL_DELETE,
    DOC_READ, DOC_WRITE, DOC_EDIT, DOC_DELETE,
    TYPING_READ, TYPING_WRITE, TYPING_EDIT, TYPING_DELETE,
    SOURCE_READ, SOURCE_WRITE, SOURCE_EDIT, SOURCE_DELETE
}
