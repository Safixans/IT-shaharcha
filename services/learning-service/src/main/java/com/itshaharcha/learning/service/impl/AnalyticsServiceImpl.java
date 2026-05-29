package com.itshaharcha.learning.service.impl;

import com.itshaharcha.learning.dto.response.DomainAnalyticsSummary;
import com.itshaharcha.learning.dto.response.MetricSeriesSet;
import com.itshaharcha.learning.dto.response.PageMeta;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.event.DomainEvent;
import com.itshaharcha.learning.repository.DocReadRepository;
import com.itshaharcha.learning.repository.EnrollmentRepository;
import com.itshaharcha.learning.repository.LessonProgressRepository;
import com.itshaharcha.learning.repository.TutorialWatchRepository;
import com.itshaharcha.learning.repository.TypingSessionRepository;
import com.itshaharcha.learning.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Learning keeps no event store, so analytics are derived from current progress state.
 * Summary rolls up completion counters; activity and metrics return well-formed empty
 * envelopes (the canonical shared analytics-api.yaml shapes).
 */
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final String DOMAIN = "learning";

    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final TutorialWatchRepository tutorialWatchRepository;
    private final DocReadRepository docReadRepository;
    private final TypingSessionRepository typingSessionRepository;

    @Override
    @Transactional(readOnly = true)
    public DomainAnalyticsSummary summary(UUID accountId, Instant from, Instant to) {
        int lessonsCompleted = (int) lessonProgressRepository.countByAccountIdAndCompletedTrue(accountId);
        int enrollments = enrollmentRepository
                .findByAccountId(accountId, Pageable.unpaged()).getNumberOfElements();
        int tutorialsCompleted = (int) tutorialWatchRepository.countByAccountIdAndCompletedTrue(accountId);
        int docsRead = (int) docReadRepository.countByAccountId(accountId);
        int typingSessions = (int) typingSessionRepository.countByAccountId(accountId);
        Map<String, Integer> counters = Map.of(
                "enrollments", enrollments,
                "lessonsCompleted", lessonsCompleted,
                "tutorialsCompleted", tutorialsCompleted,
                "docsRead", docsRead,
                "typingSessions", typingSessions);
        return new DomainAnalyticsSummary(
                accountId, DOMAIN, Instant.now(), from, to, null,
                lessonsCompleted, null, counters, null);
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
