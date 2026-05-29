package com.itshaharcha.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank @Email
        String email,

        @NotBlank
        @Size(min = 3, max = 32)
        @Pattern(regexp = "^[a-zA-Z0-9_.-]+$",
                message = "username may contain letters, digits, dot, underscore, hyphen")
        String username,

        @NotBlank
        @Size(min = 8, max = 128)
        String password,

        @Size(max = 120)
        String fullName) {
}
