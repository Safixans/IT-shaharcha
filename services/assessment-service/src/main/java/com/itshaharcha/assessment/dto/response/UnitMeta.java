package com.itshaharcha.assessment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.assessment.entity.AttemptFamily;
import com.itshaharcha.assessment.entity.SatSection;
import com.itshaharcha.assessment.entity.WritingTask;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UnitMeta(
        UUID id,
        AttemptFamily family,
        String title,
        List<String> tags,
        boolean active,
        int problemCount,
        Integer durationSeconds,
        SatSection satSection,
        WritingTask writingTask) {
}
