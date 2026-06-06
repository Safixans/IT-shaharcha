package com.itshaharcha.assessment.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ReadingCreate(
        @NotBlank String title,
        List<String> tags,
        String passage,
        @NotBlank String questions,
        Integer durationSeconds) {
}
