package com.itshaharcha.assessment.dto.request;

import jakarta.validation.Valid;

import java.util.List;

/** Optional body for POST sessions/{id}:submit — last-write answers before scoring. */
public record SubmitInput(
        @Valid List<Answer> answers) {
}
