package com.itshaharcha.identity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyRequest(

        @NotBlank @Email
        String email,

        @NotBlank
        @Size(min = 4, max = 8)
        String code) {
}
