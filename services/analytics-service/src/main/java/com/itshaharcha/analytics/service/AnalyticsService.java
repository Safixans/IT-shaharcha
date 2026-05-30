package com.itshaharcha.analytics.service;

import com.itshaharcha.analytics.dto.response.DomainAnalyticsSummary;
import com.itshaharcha.analytics.dto.response.MetricSeriesSet;
import com.itshaharcha.analytics.dto.response.PageResponse;
import com.itshaharcha.analytics.event.DomainEvent;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

/** The uniform analytics read sub-API — here answering from the cross-domain store. */
public interface AnalyticsService {

    DomainAnalyticsSummary summary(UUID accountId, Instant from, Instant to);

    PageResponse<DomainEvent> activity(UUID accountId, Instant from, Instant to, Pageable pageable);

    MetricSeriesSet metrics(UUID accountId, String metric, String granularity, Instant from, Instant to);
}
