package com.itshaharcha.learning.dto.request;

import com.itshaharcha.learning.entity.SourceTarget;
import com.itshaharcha.learning.entity.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ContentSourceInput(
        @NotBlank String name,
        @NotNull SourceType type,
        @NotNull SourceTarget target,
        @NotBlank String url,
        Boolean enabled,
        String schedule,
        String defaultTopic) {
}
