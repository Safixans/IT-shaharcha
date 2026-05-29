package com.itshaharcha.learning.dto.request;

import com.itshaharcha.learning.entity.LessonKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record LessonInput(
        @NotNull UUID moduleId,
        @NotBlank String title,
        @PositiveOrZero Integer order,
        LessonKind kind,
        @PositiveOrZero Integer estimatedMinutes,
        String body) {
}
