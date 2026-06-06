package com.itshaharcha.assessment.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/** Optimized submit: graded by value, no orderIndex. */
public record AnswerInput(
        @NotNull UUID problemId,
        List<String> values) {
}
