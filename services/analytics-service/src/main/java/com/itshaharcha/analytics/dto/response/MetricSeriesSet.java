package com.itshaharcha.analytics.dto.response;

import java.util.List;
import java.util.UUID;

/** A set of named metric series sharing one time axis (shared analytics-api.yaml). */
public record MetricSeriesSet(
        UUID accountId,
        String domain,
        String granularity,
        List<MetricSeries> series) {
}
