package com.itshaharcha.attachment.security;

import com.itshaharcha.common.exception.ApplicationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

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
}
