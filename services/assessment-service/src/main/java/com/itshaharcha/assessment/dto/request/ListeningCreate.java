package com.itshaharcha.assessment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record ListeningCreate(
        @NotBlank String title,
        List<String> tags,
        @NotBlank String questions,
        @NotNull UUID audioId,
        Integer durationSeconds) {
}
