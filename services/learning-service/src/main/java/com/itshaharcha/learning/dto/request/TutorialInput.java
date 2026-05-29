package com.itshaharcha.learning.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record TutorialInput(
        @NotBlank String title,
        String topic,
        @NotBlank String videoUrl,
        @PositiveOrZero Integer durationSeconds,
        String thumbnailUrl,
        UUID sourceId) {
}
