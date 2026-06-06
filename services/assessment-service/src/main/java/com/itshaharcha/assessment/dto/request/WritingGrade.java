package com.itshaharcha.assessment.dto.request;

import com.itshaharcha.assessment.domain.WritingCriteria;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record WritingGrade(
        @NotNull @DecimalMin("0.0") @DecimalMax("9.0") Double band,
        WritingCriteria criteria,
        String feedback) {
}
