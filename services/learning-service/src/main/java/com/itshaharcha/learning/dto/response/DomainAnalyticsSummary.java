package com.itshaharcha.learning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Compact per-domain roll-up for one account (shared analytics-api.yaml). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DomainAnalyticsSummary(
        UUID accountId,
        String domain,
        Instant generatedAt,
        Instant windowFrom,
        Instant windowTo,
        Instant lastActivityAt,
        int points,
        Integer level,
        Map<String, Integer> counters,
        Integer streakDays) {
}
