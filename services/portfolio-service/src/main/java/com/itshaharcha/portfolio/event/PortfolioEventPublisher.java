package com.itshaharcha.portfolio.event;

import com.itshaharcha.portfolio.kafka.EventPublisher;
import com.itshaharcha.portfolio.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/** Builds and emits {@link DomainEvent}s for the current actor, keyed by accountId. */
@Component
@RequiredArgsConstructor
public class PortfolioEventPublisher {

    private final EventPublisher eventPublisher;

    public void emit(String type, String subjectType, UUID subjectId, Map<String, Object> data) {
        UUID accountId = SecurityUtils.currentAccountId();
        DomainEvent event = DomainEvent.of(type, accountId, SecurityUtils.currentRoles(),
                new DomainEvent.Subject(subjectType, subjectId.toString()), data);
        eventPublisher.publish(DomainEvent.TOPIC, accountId.toString(), event);
    }
}
