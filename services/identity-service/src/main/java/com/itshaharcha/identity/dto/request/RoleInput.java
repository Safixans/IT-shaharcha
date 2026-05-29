package com.itshaharcha.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Create-role body (matches the spec Role schema used as a request). */
public record RoleInput(

        @NotBlank
        @Size(max = 64)
        String name,

        @Size(max = 255)
        String description) {
}
