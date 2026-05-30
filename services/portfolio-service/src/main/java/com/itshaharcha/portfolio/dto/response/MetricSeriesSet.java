package com.itshaharcha.portfolio.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** A set of named metric series sharing one time axis (shared analytics-api.yaml). */
public record MetricSeriesSet(
        UUID accountId,
        String domain,
        String granularity,
        List<MetricSeries> series) {

    public record MetricSeries(
            String metric,
            String unit,
            String aggregation,
            List<MetricPoint> points) {
    }

    public record MetricPoint(
            Instant t,
            double v) {
    }
}
