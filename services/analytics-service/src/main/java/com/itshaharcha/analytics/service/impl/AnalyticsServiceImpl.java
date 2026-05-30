package com.itshaharcha.analytics.service.impl;

import tools.jackson.core.type.TypeReference;
import com.itshaharcha.analytics.convert.JsonMapperHolder;
import com.itshaharcha.analytics.dto.response.DomainAnalyticsSummary;
import com.itshaharcha.analytics.dto.response.MetricSeriesSet;
import com.itshaharcha.analytics.dto.response.PageResponse;
import com.itshaharcha.analytics.entity.ProcessedEvent;
import com.itshaharcha.analytics.event.DomainEvent;
import com.itshaharcha.analytics.repository.DomainProgressRepository;
import com.itshaharcha.analytics.repository.ProcessedEventRepository;
import com.itshaharcha.analytics.service.AnalyticsService;
import com.itshaharcha.analytics.service.support.Summaries;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The shared analytics read sub-API for the analytics service itself: it answers from the
 * AGGREGATED cross-domain store. {@code summary} merges all domains; {@code activity}
 * replays the stored event stream; {@code metrics} returns a well-formed (empty) series set.
 */
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final TypeReference<Set<String>> ROLES_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<Map<String, Object>> DATA_TYPE = new TypeReference<>() {
    };

    private final DomainProgressRepository progressRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Override
    @Transactional(readOnly = true)
    public DomainAnalyticsSummary summary(UUID accountId, Instant from, Instant to) {
        return Summaries.crossDomain(accountId,
                progressRepository.findByAccountId(accountId), from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DomainEvent> activity(UUID accountId, Instant from, Instant to,
                                              Pageable pageable) {
        return PageResponse.from(
                processedEventRepository.findByAccountIdOrderByOccurredAtDesc(accountId, pageable),
                this::toEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public MetricSeriesSet metrics(UUID accountId, String metric, String granularity,
                                   Instant from, Instant to) {
        return new MetricSeriesSet(accountId, Summaries.CROSS_DOMAIN, granularity, List.of());
    }

    private DomainEvent toEvent(ProcessedEvent e) {
        Set<String> roles = parse(e.getRolesJson(), ROLES_TYPE);
        Map<String, Object> data = parse(e.getDataJson(), DATA_TYPE);
        DomainEvent.Subject subject = (e.getSubjectType() == null && e.getSubjectId() == null)
                ? null : new DomainEvent.Subject(e.getSubjectType(), e.getSubjectId());
        return new DomainEvent(
                e.getEventId(), e.getType(), e.getSource(), DomainEvent.SPEC_VERSION,
                e.getOccurredAt(), e.getRecordedAt(),
                new DomainEvent.Actor(e.getAccountId(), roles, false),
                subject, null, data);
    }

    private <T> T parse(String json, TypeReference<T> type) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return JsonMapperHolder.MAPPER.readValue(json, type);
        } catch (Exception ex) {
            return null;
        }
    }
}
