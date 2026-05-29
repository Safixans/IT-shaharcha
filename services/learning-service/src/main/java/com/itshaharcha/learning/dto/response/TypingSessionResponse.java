package com.itshaharcha.learning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TypingSessionResponse(
        UUID id,
        UUID accountId,
        double wpm,
        double accuracyPercent,
        int durationSeconds,
        Integer keystrokes,
        UUID lessonId,
        Instant createdAt) {
}
