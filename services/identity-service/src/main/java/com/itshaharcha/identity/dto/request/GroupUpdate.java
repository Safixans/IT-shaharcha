package com.itshaharcha.identity.dto.request;

import java.util.UUID;

/** Partial update; null fields are left unchanged. */
public record GroupUpdate(
        String name,
        UUID teacherId) {
}
