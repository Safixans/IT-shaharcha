package com.itshaharcha.portfolio.service.impl;

import com.itshaharcha.portfolio.dto.response.DomainAnalyticsSummary;
import com.itshaharcha.portfolio.dto.response.MetricSeriesSet;
import com.itshaharcha.portfolio.dto.response.PageMeta;
import com.itshaharcha.portfolio.dto.response.PageResponse;
import com.itshaharcha.portfolio.entity.VerificationStatus;
import com.itshaharcha.portfolio.event.DomainEvent;
import com.itshaharcha.portfolio.repository.CertificateRepository;
import com.itshaharcha.portfolio.repository.EducationRepository;
import com.itshaharcha.portfolio.repository.PortfolioItemRepository;
import com.itshaharcha.portfolio.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Portfolio keeps no event store, so analytics are derived from current state. Summary
 * rolls up counters (certificates / education / items, points = verified certificates);
 * activity and metrics return well-formed empty envelopes (the canonical shared shapes).
 */
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final String DOMAIN = "portfolio";

    private final CertificateRepository certificateRepository;
    private final EducationRepository educationRepository;
    private final PortfolioItemRepository itemRepository;

    @Override
    @Transactional(readOnly = true)
    public DomainAnalyticsSummary summary(UUID accountId, Instant from, Instant to) {
        int certificates = (int) certificateRepository.countByAccountId(accountId);
        int verified = (int) certificateRepository.countByAccountIdAndStatus(
                accountId, VerificationStatus.VERIFIED);
        int education = (int) educationRepository.countByAccountId(accountId);
        int items = (int) itemRepository.countByAccountId(accountId);
        Map<String, Integer> counters = Map.of(
                "certificates", certificates,
                "certificatesVerified", verified,
                "education", education,
                "items", items);
        return new DomainAnalyticsSummary(
                accountId, DOMAIN, Instant.now(), from, to, null,
                verified, null, counters, null);
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
