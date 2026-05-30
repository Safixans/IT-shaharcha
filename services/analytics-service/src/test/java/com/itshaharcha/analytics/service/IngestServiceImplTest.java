package com.itshaharcha.analytics.service;

import com.itshaharcha.analytics.entity.DomainProgress;
import com.itshaharcha.analytics.event.AnalyticsEventPublisher;
import com.itshaharcha.analytics.event.DomainEvent;
import com.itshaharcha.analytics.repository.DomainProgressRepository;
import com.itshaharcha.analytics.repository.MilestoneRepository;
import com.itshaharcha.analytics.repository.ProcessedEventRepository;
import com.itshaharcha.analytics.service.impl.IngestServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngestServiceImplTest {

    @Mock private ProcessedEventRepository processedEventRepository;
    @Mock private DomainProgressRepository progressRepository;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private AnalyticsEventPublisher events;
    @InjectMocks private IngestServiceImpl service;

    private final UUID accountId = UUID.randomUUID();

    private DomainEvent event(UUID eventId, String type, String source, UUID account) {
        DomainEvent.Actor actor = account == null ? null
                : new DomainEvent.Actor(account, Set.of("ROLE_STUDENT"), false);
        return new DomainEvent(eventId, type, source, "1.0", Instant.now(), Instant.now(),
                actor, new DomainEvent.Subject("lesson", "abc"), null, Map.of("k", "v"));
    }

    @Test
    void unknownType_isRejected_noWrites() {
        DomainEvent e = event(UUID.randomUUID(), "made.up.type", "learning", accountId);

        IngestService.Outcome outcome = service.ingestOne(e);

        assertThat(outcome.status()).isEqualTo(IngestService.Status.REJECTED);
        assertThat(outcome.reason()).isEqualTo("Unknown eventType");
        verify(processedEventRepository, never()).save(any());
        verify(progressRepository, never()).save(any());
    }

    @Test
    void missingActor_isRejected() {
        DomainEvent e = event(UUID.randomUUID(), "learning.lesson.completed", "learning", null);

        IngestService.Outcome outcome = service.ingestOne(e);

        assertThat(outcome.status()).isEqualTo(IngestService.Status.REJECTED);
        assertThat(outcome.reason()).isEqualTo("Missing actor.accountId");
    }

    @Test
    void duplicateEventId_isSkipped() {
        DomainEvent e = event(UUID.randomUUID(), "learning.lesson.completed", "learning", accountId);
        when(processedEventRepository.existsByEventId(e.eventId())).thenReturn(true);

        IngestService.Outcome outcome = service.ingestOne(e);

        assertThat(outcome.status()).isEqualTo(IngestService.Status.DUPLICATE);
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void accepted_foldsPointsCountersAndAwardsMilestone() {
        DomainEvent e = event(UUID.randomUUID(), "learning.lesson.completed", "learning", accountId);
        when(processedEventRepository.existsByEventId(e.eventId())).thenReturn(false);
        when(progressRepository.findByAccountIdAndDomain(accountId, "learning"))
                .thenReturn(Optional.empty());
        when(progressRepository.save(any(DomainProgress.class))).thenAnswer(i -> i.getArgument(0));
        when(milestoneRepository.existsByAccountIdAndMilestone(accountId, "first_lesson"))
                .thenReturn(false);

        IngestService.Outcome outcome = service.ingestOne(e);

        assertThat(outcome.status()).isEqualTo(IngestService.Status.ACCEPTED);
        verify(processedEventRepository).save(any());

        ArgumentCaptor<DomainProgress> captor = ArgumentCaptor.forClass(DomainProgress.class);
        verify(progressRepository).save(captor.capture());
        DomainProgress saved = captor.getValue();
        assertThat(saved.getDomain()).isEqualTo("learning");
        assertThat(saved.getPoints()).isEqualTo(50);
        assertThat(saved.getCounters()).containsEntry("lesson.completed", 1);
        assertThat(saved.getLastActivityAt()).isNotNull();

        verify(milestoneRepository).save(any());
        verify(events).emit(eq("analytics.milestone.reached"), eq(accountId), any(), any(), any());
    }

    @Test
    void accepted_existingMilestone_notReAwarded() {
        DomainEvent e = event(UUID.randomUUID(), "assessment.mock.completed", "assessment", accountId);
        when(processedEventRepository.existsByEventId(e.eventId())).thenReturn(false);
        when(progressRepository.findByAccountIdAndDomain(accountId, "assessment"))
                .thenReturn(Optional.empty());
        when(progressRepository.save(any(DomainProgress.class))).thenAnswer(i -> i.getArgument(0));
        when(milestoneRepository.existsByAccountIdAndMilestone(accountId, "first_mock_exam"))
                .thenReturn(true);
        lenient().when(milestoneRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        IngestService.Outcome outcome = service.ingestOne(e);

        assertThat(outcome.status()).isEqualTo(IngestService.Status.ACCEPTED);
        verify(milestoneRepository, never()).save(any());
        verify(events, never()).emit(any(), any(), any(), any(), any());
    }
}
