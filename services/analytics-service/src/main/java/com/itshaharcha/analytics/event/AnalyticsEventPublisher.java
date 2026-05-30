package com.itshaharcha.analytics.event;

import com.itshaharcha.analytics.kafka.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Emits the analytics service's own derived {@link DomainEvent}s (e.g. milestone.reached). */
@Component
@RequiredArgsConstructor
public class AnalyticsEventPublisher {

    private final EventPublisher eventPublisher;

    public void emit(String type, UUID accountId, String subjectType, String subjectId,
                     Map<String, Object> data) {
        DomainEvent event = DomainEvent.analytics(type, accountId, Set.of(),
                new DomainEvent.Subject(subjectType, subjectId), data);
        eventPublisher.publish(DomainEvent.TOPIC, accountId.toString(), event);
    }
}
