package com.itshaharcha.identity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record GroupInput(
        @NotBlank String name,
        @NotNull UUID teacherId) {
}
