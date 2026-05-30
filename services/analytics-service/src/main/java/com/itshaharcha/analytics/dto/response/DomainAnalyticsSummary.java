package com.itshaharcha.analytics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** Per-domain (or cross-domain) roll-up for one account (shared analytics-api.yaml). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DomainAnalyticsSummary(
        UUID accountId,
        String domain,
        Instant generatedAt,
        Instant windowFrom,
        Instant windowTo,
        Instant lastActivityAt,
        long points,
        Integer level,
        Map<String, Integer> counters,
        Integer streakDays) {
}
