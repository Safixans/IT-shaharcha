package com.itshaharcha.analytics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Aggregated dashboard payload for one account (spec Dashboard). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Dashboard(
        UUID accountId,
        Instant generatedAt,
        List<DomainAnalyticsSummary> summaries,
        List<MetricSeries> series,
        RankEntry rank) {
}
