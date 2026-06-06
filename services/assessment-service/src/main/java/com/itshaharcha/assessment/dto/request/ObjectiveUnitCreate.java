package com.itshaharcha.assessment.dto.request;

import com.itshaharcha.assessment.entity.SatSection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ObjectiveUnitCreate(
        @NotBlank String title,
        List<String> tags,
        SatSection satSection,
        Integer durationSeconds,
        @NotEmpty List<ObjectiveQuestionInput> questions) {
}
