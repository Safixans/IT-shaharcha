package com.itshaharcha.learning.dto.request;

import com.itshaharcha.learning.entity.Level;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record CourseInput(
        UUID trackId,
        @NotBlank String title,
        String slug,
        String summary,
        @NotNull Level level,
        @PositiveOrZero Integer estimatedMinutes) {
}
