package com.itshaharcha.assessment.service.impl;

import com.itshaharcha.assessment.dto.response.DomainAnalyticsSummary;
import com.itshaharcha.assessment.dto.response.MetricSeriesSet;
import com.itshaharcha.assessment.dto.response.PageMeta;
import com.itshaharcha.assessment.dto.response.PageResponse;
import com.itshaharcha.assessment.event.DomainEvent;
import com.itshaharcha.assessment.repository.ExamResultRepository;
import com.itshaharcha.assessment.repository.ExamSessionRepository;
import com.itshaharcha.assessment.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Assessment keeps no event store, so analytics are derived from current session/result
 * state. Summary rolls up counters; activity and metrics return well-formed empty
 * envelopes (the canonical shared analytics-api.yaml shapes).
 */
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final String DOMAIN = "assessment";

    private final ExamSessionRepository sessionRepository;
    private final ExamResultRepository resultRepository;

    @Override
    @Transactional(readOnly = true)
    public DomainAnalyticsSummary summary(UUID accountId, Instant from, Instant to) {
        int sessions = (int) sessionRepository.countByAccountId(accountId);
        int results = (int) resultRepository.countByAccountId(accountId);
        Map<String, Integer> counters = Map.of(
                "sessions", sessions,
                "examsScored", results);
        return new DomainAnalyticsSummary(
                accountId, DOMAIN, Instant.now(), from, to, null,
                results, null, counters, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DomainEvent> activity(UUID accountId, Instant from, Instant to, Pageable pageable) {
        PageMeta meta = new PageMeta(pageable.getPageNumber(), pageable.getPageSize(), 0L, 0, false);
        return new PageResponse<>(List.of(), meta);
    }

    @Override
    @Transactional(readOnly = true)
    public MetricSeriesSet metrics(UUID accountId, String metric, String granularity,
                                   Instant from, Instant to) {
        return new MetricSeriesSet(accountId, DOMAIN, granularity, List.of());
    }
}
