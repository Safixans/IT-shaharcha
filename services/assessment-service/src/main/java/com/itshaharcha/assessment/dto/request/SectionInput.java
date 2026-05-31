package com.itshaharcha.assessment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record SectionInput(
        @NotBlank String name,
        @PositiveOrZero Integer order,
        @PositiveOrZero Integer durationMinutes,
        String content,
        String pdfUrl) {
}
