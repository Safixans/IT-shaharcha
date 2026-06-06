package com.itshaharcha.assessment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.assessment.entity.AttemptFamily;
import com.itshaharcha.assessment.entity.AttemptStatus;

import java.util.List;
import java.util.UUID;

/** A started/resumed attempt: snapshotted content (answer-safe) + timezone-proof timing. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AttemptSession(
        UUID attemptId,
        UUID unitId,
        AttemptFamily family,
        AttemptStatus status,
        String title,
        Timing timing,
        String sectionData,
        String passage,
        String prompt,
        UUID audioId,
        UUID imageId,
        List<ServedProblem> problems) {
}
