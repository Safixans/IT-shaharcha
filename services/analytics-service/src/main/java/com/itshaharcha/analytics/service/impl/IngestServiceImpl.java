package com.itshaharcha.analytics.service.impl;

import com.itshaharcha.analytics.convert.JsonMapperHolder;
import com.itshaharcha.analytics.dto.response.IngestResult;
import com.itshaharcha.analytics.entity.DomainProgress;
import com.itshaharcha.analytics.entity.MilestoneRecord;
import com.itshaharcha.analytics.entity.ProcessedEvent;
import com.itshaharcha.analytics.event.AnalyticsEventPublisher;
import com.itshaharcha.analytics.event.DomainEvent;
import com.itshaharcha.analytics.repository.DomainProgressRepository;
import com.itshaharcha.analytics.repository.MilestoneRepository;
import com.itshaharcha.analytics.repository.ProcessedEventRepository;
import com.itshaharcha.analytics.scoring.EventScoring;
import com.itshaharcha.analytics.service.IngestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Folds DomainEvents into the cross-domain store. Idempotent on {@code eventId}: a
 * re-delivered event is a no-op (deduped via the unique processed_events row). Each
 * accepted event bumps the actor's per-domain points/counters and may award a milestone.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IngestServiceImpl implements IngestService {

    private static final int MILESTONE_BONUS = 25;

    private final ProcessedEventRepository processedEventRepository;
    private final DomainProgressRepository progressRepository;
    private final MilestoneRepository milestoneRepository;
    private final AnalyticsEventPublisher events;

    @Override
    @Transactional
    public IngestResult ingest(List<DomainEvent> incoming) {
        int accepted = 0;
        int duplicates = 0;
        List<IngestResult.Rejected> rejected = new ArrayList<>();
        for (DomainEvent event : incoming) {
            Outcome outcome = doIngest(event);
            switch (outcome.status()) {
                case ACCEPTED -> accepted++;
                case DUPLICATE -> duplicates++;
                case REJECTED -> rejected.add(new IngestResult.Rejected(
                        event == null ? null : event.eventId(), outcome.reason()));
            }
        }
        return new IngestResult(incoming.size(), accepted, duplicates, rejected);
    }

    @Override
    @Transactional
    public Outcome ingestOne(DomainEvent event) {
        return doIngest(event);
    }

    /** Core fold; runs inside the caller's transaction (batch tx or the consumer's per-event tx). */
    private Outcome doIngest(DomainEvent event) {
        String reason = validate(event);
        if (reason != null) {
            return Outcome.rejected(reason);
        }
        if (processedEventRepository.existsByEventId(event.eventId())) {
            return Outcome.duplicate();
        }

        UUID accountId = event.actor().accountId();
        String domain = event.source();
        String type = event.type();
        Instant occurredAt = event.occurredAt() != null ? event.occurredAt() : Instant.now();

        persistEvent(event, accountId, occurredAt);
        foldProgress(accountId, domain, type, occurredAt);
        awardMilestone(accountId, domain, type, occurredAt);
        return Outcome.accepted();
    }

    private String validate(DomainEvent event) {
        if (event == null) {
            return "Empty event";
        }
        if (event.eventId() == null) {
            return "Missing eventId";
        }
        if (!EventScoring.isRecognized(event.type())) {
            return "Unknown eventType";
        }
        if (event.actor() == null || event.actor().accountId() == null) {
            return "Missing actor.accountId";
        }
        return null;
    }

    private void persistEvent(DomainEvent event, UUID accountId, Instant occurredAt) {
        ProcessedEvent row = new ProcessedEvent();
        row.setEventId(event.eventId());
        row.setType(event.type());
        row.setSource(event.source());
        row.setAccountId(accountId);
        row.setOccurredAt(occurredAt);
        row.setRecordedAt(event.recordedAt() != null ? event.recordedAt() : Instant.now());
        if (event.subject() != null) {
            row.setSubjectType(event.subject().type());
            row.setSubjectId(event.subject().id());
        }
        row.setRolesJson(toJson(event.actor().roles()));
        row.setDataJson(toJson(event.data()));
        processedEventRepository.save(row);
    }

    private void foldProgress(UUID accountId, String domain, String type, Instant occurredAt) {
        DomainProgress progress = progressRepository.findByAccountIdAndDomain(accountId, domain)
                .orElseGet(() -> {
                    DomainProgress p = new DomainProgress();
                    p.setAccountId(accountId);
                    p.setDomain(domain);
                    p.setCounters(new LinkedHashMap<>());
                    return p;
                });
        progress.setPoints(progress.getPoints() + EventScoring.pointsFor(type));
        progress.setLevel(EventScoring.levelFor(progress.getPoints()));

        Map<String, Integer> counters = progress.getCounters();
        String key = EventScoring.counterKey(type, domain);
        counters.merge(key, 1, Integer::sum);
        progress.setCounters(counters);

        if (progress.getLastActivityAt() == null || occurredAt.isAfter(progress.getLastActivityAt())) {
            progress.setLastActivityAt(occurredAt);
        }
        progressRepository.save(progress);
    }

    private void awardMilestone(UUID accountId, String domain, String type, Instant occurredAt) {
        String milestone = EventScoring.milestoneFor(type);
        if (milestone == null
                || milestoneRepository.existsByAccountIdAndMilestone(accountId, milestone)) {
            return;
        }
        MilestoneRecord record = new MilestoneRecord();
        record.setAccountId(accountId);
        record.setMilestone(milestone);
        record.setPoints(MILESTONE_BONUS);
        record.setDomain(domain);
        record.setReachedAt(occurredAt);
        milestoneRepository.save(record);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("milestone", milestone);
        data.put("points", MILESTONE_BONUS);
        data.put("domain", domain);
        events.emit("analytics.milestone.reached", accountId, "milestone", milestone, data);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return JsonMapperHolder.MAPPER.writeValueAsString(value);
        } catch (Exception ex) {
            log.warn("Could not serialize event payload: {}", ex.getMessage());
            return null;
        }
    }
}
