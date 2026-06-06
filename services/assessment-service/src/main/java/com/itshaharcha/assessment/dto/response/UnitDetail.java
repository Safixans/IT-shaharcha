package com.itshaharcha.assessment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.assessment.entity.AttemptFamily;
import com.itshaharcha.assessment.entity.SatSection;
import com.itshaharcha.assessment.entity.WritingTask;

import java.util.List;
import java.util.UUID;

/** Authoring view of a unit, including its answer-stripped served content. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UnitDetail(
        UUID id,
        AttemptFamily family,
        String title,
        List<String> tags,
        boolean active,
        int problemCount,
        Integer durationSeconds,
        SatSection satSection,
        WritingTask writingTask,
        String sectionData,
        // Authored HTML WITH answer markers — populated only for authors (used to edit/re-parse).
        String originalSectionData,
        String passage,
        String prompt,
        UUID audioId,
        UUID imageId,
        List<ServedProblem> problems) {
}
