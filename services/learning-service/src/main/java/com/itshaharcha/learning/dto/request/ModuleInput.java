package com.itshaharcha.learning.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record ModuleInput(
        @NotNull UUID courseId,
        @NotBlank String title,
        @PositiveOrZero Integer order) {
}
