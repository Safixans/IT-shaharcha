package com.itshaharcha.learning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LessonProgressResponse(
        UUID lessonId,
        UUID courseId,
        UUID accountId,
        boolean completed,
        Double scorePercent,
        double courseProgressPercent,
        Instant completedAt) {
}
