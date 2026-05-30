package com.itshaharcha.assessment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.assessment.entity.SessionStatus;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExamSessionResponse(
        UUID id,
        UUID examId,
        UUID accountId,
        SessionStatus status,
        Instant startedAt,
        Instant expiresAt,
        Instant submittedAt) {
}
