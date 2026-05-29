package com.itshaharcha.auth.dto.response;

import com.itshaharcha.auth.entity.AccountStatus;
import com.itshaharcha.auth.entity.AuthProvider;

import java.util.Set;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String email,
        String username,
        AccountStatus status,
        boolean emailVerified,
        AuthProvider provider,
        Set<String> roles) {
}
