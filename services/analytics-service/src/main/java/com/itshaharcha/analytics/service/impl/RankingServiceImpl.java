package com.itshaharcha.analytics.service.impl;

import com.itshaharcha.analytics.dto.response.Leaderboard;
import com.itshaharcha.analytics.dto.response.PageMeta;
import com.itshaharcha.analytics.dto.response.RankEntry;
import com.itshaharcha.analytics.entity.DomainProgress;
import com.itshaharcha.analytics.repository.AccountPoints;
import com.itshaharcha.analytics.repository.DomainProgressRepository;
import com.itshaharcha.analytics.scoring.EventScoring;
import com.itshaharcha.analytics.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Leaderboards computed from the folded {@code domain_progress} store. {@code period} is
 * accepted per the spec; this implementation ranks on the cumulative (all-time) store, so
 * the period is reflected back as a label rather than re-windowing the points.
 */
@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final DomainProgressRepository progressRepository;

    @Override
    @Transactional(readOnly = true)
    public Leaderboard leaderboard(String domain, String period, Pageable pageable) {
        String resolvedPeriod = period == null || period.isBlank() ? "all_time" : period;
        List<RankEntry> entries = new ArrayList<>();
        PageMeta meta;
        long base = (long) pageable.getPageNumber() * pageable.getPageSize();

        if (domain != null && !domain.isBlank()) {
            Page<DomainProgress> page =
                    progressRepository.findByDomainOrderByPointsDescAccountIdAsc(domain, pageable);
            int rank = (int) base + 1;
            for (DomainProgress p : page.getContent()) {
                entries.add(entry(rank++, p.getAccountId(), domain, p.getPoints(), p.getLevel()));
            }
            meta = new PageMeta(page.getNumber(), page.getSize(),
                    page.getTotalElements(), page.getTotalPages(), page.hasNext());
        } else {
            List<AccountPoints> board = progressRepository.overallBoard(pageable);
            int rank = (int) base + 1;
            for (AccountPoints ap : board) {
                entries.add(entry(rank++, ap.getAccountId(), null,
                        ap.getPoints(), EventScoring.levelFor(ap.getPoints())));
            }
            boolean hasNext = board.size() == pageable.getPageSize();
            meta = new PageMeta(pageable.getPageNumber(), pageable.getPageSize(),
                    base + board.size(), pageable.getPageNumber() + (hasNext ? 2 : 1), hasNext);
        }
        return new Leaderboard(domain, resolvedPeriod, Instant.now(), entries, meta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankEntry> myRank(UUID accountId, String period) {
        List<DomainProgress> mine = progressRepository.findByAccountId(accountId);
        List<RankEntry> entries = new ArrayList<>();

        long total = mine.stream().mapToLong(DomainProgress::getPoints).sum();
        int overallRank = (int) progressRepository
                .countAccountsWithOverallPointsGreaterThan(total) + 1;
        entries.add(entry(overallRank, accountId, null, total, EventScoring.levelFor(total)));

        for (DomainProgress p : mine) {
            int rank = (int) progressRepository
                    .countByDomainAndPointsGreaterThan(p.getDomain(), p.getPoints()) + 1;
            entries.add(entry(rank, accountId, p.getDomain(), p.getPoints(), p.getLevel()));
        }
        return entries;
    }

    private RankEntry entry(int rank, UUID accountId, String domain, long points, Integer level) {
        // username/displayName/avatar are owned by identity-service; left null here.
        return new RankEntry(rank, accountId, null, null, null, domain, points, level, null);
    }
}
