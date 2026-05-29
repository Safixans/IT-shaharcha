package com.itshaharcha.learning.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TrackInput(
        @NotBlank String title,
        String slug,
        String description) {
}
