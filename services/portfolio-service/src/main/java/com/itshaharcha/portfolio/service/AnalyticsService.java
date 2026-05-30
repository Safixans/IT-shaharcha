package com.itshaharcha.portfolio.service;

import com.itshaharcha.portfolio.dto.response.DomainAnalyticsSummary;
import com.itshaharcha.portfolio.dto.response.MetricSeriesSet;
import com.itshaharcha.portfolio.dto.response.PageResponse;
import com.itshaharcha.portfolio.event.DomainEvent;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface AnalyticsService {

    DomainAnalyticsSummary summary(UUID accountId, Instant from, Instant to);

    PageResponse<DomainEvent> activity(UUID accountId, Instant from, Instant to, Pageable pageable);

    MetricSeriesSet metrics(UUID accountId, String metric, String granularity, Instant from, Instant to);
}
