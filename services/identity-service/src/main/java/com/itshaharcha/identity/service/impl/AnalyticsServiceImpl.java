package com.itshaharcha.identity.service.impl;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.identity.dto.response.DomainAnalyticsSummary;
import com.itshaharcha.identity.dto.response.MetricSeriesSet;
import com.itshaharcha.identity.dto.response.PageMeta;
import com.itshaharcha.identity.dto.response.PageResponse;
import com.itshaharcha.identity.entity.Account;
import com.itshaharcha.identity.event.DomainEvent;
import com.itshaharcha.identity.repository.AccountRepository;
import com.itshaharcha.identity.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Identity owns no event store, so analytics are derived from current account state.
 * Summary reflects verification/registration milestones; activity and metrics return
 * well-formed empty envelopes (the canonical shared analytics-api.yaml shapes).
 */
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final String DOMAIN = "identity";

    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public DomainAnalyticsSummary summary(UUID accountId, Instant from, Instant to) {
        Account account = require(accountId);
        Instant lastActivity = account.getUpdatedAt() != null
                ? account.getUpdatedAt() : account.getCreatedAt();
        Map<String, Integer> counters = Map.of(
                "roles", account.getRoles().size(),
                "emailVerified", account.isEmailVerified() ? 1 : 0);
        return new DomainAnalyticsSummary(
                accountId, DOMAIN, Instant.now(), from, to, lastActivity,
                0, null, counters, null);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DomainEvent> activity(UUID accountId, Instant from, Instant to,
                                              Pageable pageable) {
        require(accountId);
        PageMeta meta = new PageMeta(pageable.getPageNumber(), pageable.getPageSize(), 0L, 0, false);
        return new PageResponse<>(List.of(), meta);
    }

    @Override
    @Transactional(readOnly = true)
    public MetricSeriesSet metrics(UUID accountId, String metric, String granularity,
                                   Instant from, Instant to) {
        require(accountId);
        return new MetricSeriesSet(accountId, DOMAIN, granularity, List.of());
    }

    private Account require(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> ApplicationException.notFound("Account not found"));
    }
}
