package com.itshaharcha.assessment.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** A single candidate answer (spec Answer). */
public record Answer(
        @NotNull UUID questionId,
        @NotNull Object value,
        UUID sectionId) {
}
