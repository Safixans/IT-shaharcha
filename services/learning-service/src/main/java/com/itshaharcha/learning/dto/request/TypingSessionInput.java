package com.itshaharcha.learning.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

/** Body for POST typing/sessions (shared events TypingSessionCompletedData). */
public record TypingSessionInput(
        @NotNull @PositiveOrZero Double wpm,
        @NotNull @DecimalMin("0") @DecimalMax("100") Double accuracyPercent,
        @NotNull @PositiveOrZero Integer durationSeconds,
        @PositiveOrZero Integer keystrokes,
        UUID lessonId) {
}
