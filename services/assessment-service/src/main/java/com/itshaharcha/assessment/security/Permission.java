package com.itshaharcha.assessment.security;

/**
 * Per-resource authoring permissions, granted to a request as
 * {@code GrantedAuthority}s based on the caller's roles (see {@link RolePermissions}).
 * Naming: {@code <RESOURCE>_<ACTION>} with ACTION in {READ, WRITE, EDIT, DELETE}.
 * Used by {@code @PreAuthorize("hasAuthority('EXAM_WRITE')")} on admin endpoints.
 */
public enum Permission {
    EXAM_READ, EXAM_WRITE, EXAM_EDIT, EXAM_DELETE,
    SECTION_READ, SECTION_WRITE, SECTION_EDIT, SECTION_DELETE,
    QUESTION_READ, QUESTION_WRITE, QUESTION_EDIT, QUESTION_DELETE
}
