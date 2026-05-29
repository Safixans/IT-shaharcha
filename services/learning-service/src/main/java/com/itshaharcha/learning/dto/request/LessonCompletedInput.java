package com.itshaharcha.learning.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

/** Body for POST lessons/{id}:complete (shared events LessonCompletedData). */
public record LessonCompletedInput(
        @NotNull UUID courseId,
        UUID moduleId,
        @NotNull @PositiveOrZero Integer durationSeconds,
        @DecimalMin("0") @DecimalMax("100") Double scorePercent) {
}
