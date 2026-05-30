package com.itshaharcha.analytics.service.impl;

import com.itshaharcha.analytics.dto.response.Dashboard;
import com.itshaharcha.analytics.dto.response.DomainAnalyticsSummary;
import com.itshaharcha.analytics.dto.response.RankEntry;
import com.itshaharcha.analytics.entity.DomainProgress;
import com.itshaharcha.analytics.repository.DomainProgressRepository;
import com.itshaharcha.analytics.scoring.EventScoring;
import com.itshaharcha.analytics.service.DashboardService;
import com.itshaharcha.analytics.service.support.Summaries;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DomainProgressRepository progressRepository;

    @Override
    @Transactional(readOnly = true)
    public Dashboard dashboard(UUID accountId, String granularity, Instant from, Instant to) {
        List<DomainProgress> rows = progressRepository.findByAccountId(accountId);
        List<DomainAnalyticsSummary> summaries = rows.stream()
                .map(p -> Summaries.forDomain(p, from, to))
                .toList();

        long total = rows.stream().mapToLong(DomainProgress::getPoints).sum();
        int overallRank = (int) progressRepository
                .countAccountsWithOverallPointsGreaterThan(total) + 1;
        RankEntry rank = new RankEntry(overallRank, accountId, null, null, null,
                null, total, EventScoring.levelFor(total), null);

        // Cross-domain charting series are not yet materialized; return a well-formed empty set.
        return new Dashboard(accountId, Instant.now(), summaries, List.of(), rank);
    }
}
