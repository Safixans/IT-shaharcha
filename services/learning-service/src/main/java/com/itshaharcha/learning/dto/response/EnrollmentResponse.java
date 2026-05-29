package com.itshaharcha.learning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.learning.entity.EnrollmentStatus;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EnrollmentResponse(
        UUID id,
        UUID courseId,
        UUID accountId,
        EnrollmentStatus status,
        double progressPercent,
        Instant enrolledAt,
        Instant completedAt) {
}
