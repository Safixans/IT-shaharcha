package com.itshaharcha.learning.dto.request;

import com.itshaharcha.learning.entity.Level;
import jakarta.validation.constraints.NotBlank;

public record TypingLessonInput(
        @NotBlank String title,
        Level difficulty,
        @NotBlank String text) {
}
