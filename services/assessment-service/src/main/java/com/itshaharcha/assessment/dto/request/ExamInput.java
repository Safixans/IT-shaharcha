package com.itshaharcha.assessment.dto.request;

import com.itshaharcha.assessment.entity.ExamType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ExamInput(
        @NotBlank String title,
        @NotNull ExamType examType,
        String description,
        @PositiveOrZero Integer durationMinutes,
        Boolean isRealExam) {
}
