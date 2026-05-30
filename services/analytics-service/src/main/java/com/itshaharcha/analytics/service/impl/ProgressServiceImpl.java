package com.itshaharcha.analytics.service.impl;

import com.itshaharcha.analytics.dto.response.DomainAnalyticsSummary;
import com.itshaharcha.analytics.dto.response.Milestone;
import com.itshaharcha.analytics.dto.response.PageResponse;
import com.itshaharcha.analytics.dto.response.ProgressOverview;
import com.itshaharcha.analytics.entity.DomainProgress;
import com.itshaharcha.analytics.repository.DomainProgressRepository;
import com.itshaharcha.analytics.repository.MilestoneRepository;
import com.itshaharcha.analytics.scoring.EventScoring;
import com.itshaharcha.analytics.service.ProgressService;
import com.itshaharcha.analytics.service.support.Summaries;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final DomainProgressRepository progressRepository;
    private final MilestoneRepository milestoneRepository;

    @Override
    @Transactional(readOnly = true)
    public ProgressOverview progress(UUID accountId, Instant from, Instant to) {
        List<DomainProgress> rows = progressRepository.findByAccountId(accountId);
        List<DomainAnalyticsSummary> domains = rows.stream()
                .map(p -> Summaries.forDomain(p, from, to))
                .toList();
        long totalPoints = rows.stream().mapToLong(DomainProgress::getPoints).sum();
        return new ProgressOverview(
                accountId, Instant.now(), totalPoints,
                EventScoring.levelFor(totalPoints), null, domains);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<Milestone> milestones(UUID accountId, Pageable pageable) {
        return PageResponse.from(
                milestoneRepository.findByAccountIdOrderByReachedAtDesc(accountId, pageable),
                m -> new Milestone(m.getMilestone(), m.getPoints(), m.getDomain(),
                        m.getAccountId(), m.getReachedAt()));
    }
}
