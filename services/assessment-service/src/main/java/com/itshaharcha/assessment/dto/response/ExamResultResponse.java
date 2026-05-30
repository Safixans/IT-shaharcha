package com.itshaharcha.assessment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.assessment.dto.SectionScore;
import com.itshaharcha.assessment.entity.ExamType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Scored result (spec ExamResult = ExamScoredData + session fields). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExamResultResponse(
        UUID examId,
        ExamType examType,
        double scaledScore,
        Double maxScore,
        List<SectionScore> sectionScores,
        UUID sessionId,
        UUID accountId,
        Instant scoredAt,
        String feedback) {
}
