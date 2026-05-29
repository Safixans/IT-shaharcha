package com.itshaharcha.identity.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        /** Username or email. */
        @NotBlank
        String identifier,

        @NotBlank
        String password) {
}
