package com.itshaharcha.identity.dto.response;

import com.itshaharcha.identity.entity.AccountStatus;
import com.itshaharcha.identity.entity.AuthProvider;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Mirrors the spec Account schema. */
public record AccountResponse(
        UUID id,
        String email,
        String username,
        AccountStatus status,
        boolean emailVerified,
        AuthProvider provider,
        List<String> roles,
        Instant createdAt,
        Instant updatedAt) {
}
