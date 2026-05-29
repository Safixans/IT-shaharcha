package com.itshaharcha.learning.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record DocInput(
        @NotBlank String title,
        String topic,
        String url,
        String body,
        @PositiveOrZero Integer estimatedMinutes,
        UUID sourceId) {
}
