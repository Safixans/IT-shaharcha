package com.itshaharcha.assessment.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Body for PUT sessions/{id}/answers. */
public record SaveAnswersInput(
        @NotNull @Valid List<Answer> answers) {
}
