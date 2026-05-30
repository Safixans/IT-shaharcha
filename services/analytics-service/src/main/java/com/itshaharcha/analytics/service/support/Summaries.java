package com.itshaharcha.analytics.service.support;

import com.itshaharcha.analytics.dto.response.DomainAnalyticsSummary;
import com.itshaharcha.analytics.entity.DomainProgress;
import com.itshaharcha.analytics.scoring.EventScoring;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Builds the shared {@link DomainAnalyticsSummary} shape from folded progress rows. */
public final class Summaries {

    public static final String CROSS_DOMAIN = "analytics";

    private Summaries() {
    }

    /** One domain's progress row -> its summary. */
    public static DomainAnalyticsSummary forDomain(DomainProgress p, Instant from, Instant to) {
        return new DomainAnalyticsSummary(
                p.getAccountId(), p.getDomain(), Instant.now(), from, to,
                p.getLastActivityAt(), p.getPoints(), p.getLevel(),
                p.getCounters(), p.getStreakDays());
    }

    /** Merge every domain into one cross-domain summary (the analytics sub-API answer). */
    public static DomainAnalyticsSummary crossDomain(UUID accountId, List<DomainProgress> rows,
                                                     Instant from, Instant to) {
        long totalPoints = 0;
        Instant lastActivity = null;
        Map<String, Integer> counters = new LinkedHashMap<>();
        for (DomainProgress p : rows) {
            totalPoints += p.getPoints();
            if (p.getLastActivityAt() != null
                    && (lastActivity == null || p.getLastActivityAt().isAfter(lastActivity))) {
                lastActivity = p.getLastActivityAt();
            }
            p.getCounters().forEach((k, v) ->
                    counters.merge(p.getDomain() + "." + k, v, Integer::sum));
        }
        return new DomainAnalyticsSummary(
                accountId, CROSS_DOMAIN, Instant.now(), from, to, lastActivity,
                totalPoints, EventScoring.levelFor(totalPoints), counters, null);
    }
}
