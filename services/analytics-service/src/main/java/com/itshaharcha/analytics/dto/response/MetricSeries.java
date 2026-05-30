package com.itshaharcha.analytics.dto.response;

import java.time.Instant;
import java.util.List;

/** A single named metric as an ordered list of time buckets (shared analytics-api.yaml). */
public record MetricSeries(
        String metric,
        String unit,
        String aggregation,
        List<MetricPoint> points) {

    public record MetricPoint(
            Instant t,
            double v) {
    }
}
