package com.itshaharcha.analytics.kafka;

import com.itshaharcha.analytics.event.DomainEvent;
import com.itshaharcha.analytics.service.IngestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * The platform's primary event consumer. Subscribes to every {@code itsh.<service>.events}
 * topic and folds each {@link DomainEvent} into the cross-domain analytics store via
 * {@link IngestService} (idempotent on {@code eventId}). Active only when a broker is
 * configured ({@code app.events.kafka-enabled=true}); otherwise events arrive over the
 * REST ingest fallback.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.events", name = "kafka-enabled", havingValue = "true")
public class KafkaEventConsumer {

    private final IngestService ingestService;

    @KafkaListener(
            topics = {
                    "itsh.identity.events",
                    "itsh.learning.events",
                    "itsh.assessment.events",
                    "itsh.portfolio.events"
            },
            groupId = "${spring.kafka.consumer.group-id:analytics-service}")
    public void onEvent(DomainEvent event) {
        if (event == null) {
            return;
        }
        try {
            IngestService.Outcome outcome = ingestService.ingestOne(event);
            log.debug("Consumed event {} ({}) -> {}", event.eventId(), event.type(), outcome);
        } catch (Exception ex) {
            // Swallow so a single poison message can't stall the partition; it is logged
            // for investigation. (A production system would route to a dead-letter topic.)
            log.error("Failed to process event {}: {}",
                    event.eventId(), ex.getMessage(), ex);
        }
    }
}
