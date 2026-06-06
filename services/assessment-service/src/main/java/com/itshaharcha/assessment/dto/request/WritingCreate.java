package com.itshaharcha.assessment.dto.request;

import com.itshaharcha.assessment.entity.WritingTask;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record WritingCreate(
        @NotBlank String title,
        List<String> tags,
        @NotNull WritingTask task,
        @NotBlank String prompt,
        UUID imageId,
        Integer durationSeconds) {
}
