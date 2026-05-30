package com.itshaharcha.analytics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Cross-domain progress roll-up for one account (spec ProgressOverview). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProgressOverview(
        UUID accountId,
        Instant generatedAt,
        long totalPoints,
        Integer overallLevel,
        Integer streakDays,
        List<DomainAnalyticsSummary> domains) {
}
