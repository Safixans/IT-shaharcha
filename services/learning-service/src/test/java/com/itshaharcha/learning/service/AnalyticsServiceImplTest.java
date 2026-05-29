package com.itshaharcha.learning.service;

import com.itshaharcha.learning.dto.response.DomainAnalyticsSummary;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.event.DomainEvent;
import com.itshaharcha.learning.repository.DocReadRepository;
import com.itshaharcha.learning.repository.EnrollmentRepository;
import com.itshaharcha.learning.repository.LessonProgressRepository;
import com.itshaharcha.learning.repository.TutorialWatchRepository;
import com.itshaharcha.learning.repository.TypingSessionRepository;
import com.itshaharcha.learning.service.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private TutorialWatchRepository tutorialWatchRepository;
    @Mock private DocReadRepository docReadRepository;
    @Mock private TypingSessionRepository typingSessionRepository;
    @InjectMocks private AnalyticsServiceImpl service;

    @Test
    void summary_rollsUpCompletionCountersWithPointsFromLessonsCompleted() {
        UUID accountId = UUID.randomUUID();
        when(lessonProgressRepository.countByAccountIdAndCompletedTrue(accountId)).thenReturn(4L);
        Page<com.itshaharcha.learning.entity.Enrollment> enrollments =
                new PageImpl<>(List.of(new com.itshaharcha.learning.entity.Enrollment(),
                        new com.itshaharcha.learning.entity.Enrollment()));
        when(enrollmentRepository.findByAccountId(any(), any())).thenReturn(enrollments);
        when(tutorialWatchRepository.countByAccountIdAndCompletedTrue(accountId)).thenReturn(1L);
        when(docReadRepository.countByAccountId(accountId)).thenReturn(3L);
        when(typingSessionRepository.countByAccountId(accountId)).thenReturn(5L);

        DomainAnalyticsSummary summary = service.summary(accountId, null, null);

        assertThat(summary.domain()).isEqualTo("learning");
        assertThat(summary.points()).isEqualTo(4);
        assertThat(summary.counters())
                .containsEntry("enrollments", 2)
                .containsEntry("lessonsCompleted", 4)
                .containsEntry("tutorialsCompleted", 1)
                .containsEntry("docsRead", 3)
                .containsEntry("typingSessions", 5);
    }

    @Test
    void activity_returnsEmptyEnvelope() {
        UUID accountId = UUID.randomUUID();
        PageResponse<DomainEvent> page = service.activity(accountId, null, null, PageRequest.of(0, 20));
        assertThat(page.items()).isEmpty();
        assertThat(page.meta().totalElements()).isZero();
    }

    @Test
    void metrics_returnsEmptySeriesForDomain() {
        UUID accountId = UUID.randomUUID();
        var set = service.metrics(accountId, "wpm", "day", null, null);
        assertThat(set.domain()).isEqualTo("learning");
        assertThat(set.series()).isEmpty();
    }
}
