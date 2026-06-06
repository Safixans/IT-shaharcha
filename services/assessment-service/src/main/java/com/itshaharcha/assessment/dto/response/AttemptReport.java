package com.itshaharcha.assessment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.assessment.domain.WritingCriteria;
import com.itshaharcha.assessment.entity.AttemptFamily;
import com.itshaharcha.assessment.entity.AttemptStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AttemptReport(
        UUID attemptId,
        UUID unitId,
        UUID studentId,
        AttemptFamily family,
        String title,
        AttemptStatus status,
        int correct,
        int incorrect,
        int total,
        Double scorePercent,
        Double band,
        List<AnswerReport> answers,
        String essay,
        String feedback,
        WritingCriteria criteria,
        Instant startedAt,
        Instant submittedAt,
        Instant gradedAt) {
}
