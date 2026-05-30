package com.itshaharcha.assessment.event;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Canonical platform event envelope (shared/events.yaml). Assessment emits these to
 * the {@code itsh.assessment.events} topic, keyed by {@code actor.accountId}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DomainEvent(
        UUID eventId,
        String type,
        String source,
        String specVersion,
        Instant occurredAt,
        Instant recordedAt,
        Actor actor,
        Subject subject,
        Map<String, Object> data) {

    public static final String TOPIC = "itsh.assessment.events";
    public static final String SOURCE = "assessment";
    public static final String SPEC_VERSION = "1.0";

    public record Actor(UUID accountId, Set<String> roles) {
    }

    public record Subject(String type, String id) {
    }

    public static DomainEvent of(String type, UUID accountId, Set<String> roles,
                                 Subject subject, Map<String, Object> data) {
        return new DomainEvent(UUID.randomUUID(), type, SOURCE, SPEC_VERSION,
                Instant.now(), Instant.now(), new Actor(accountId, roles), subject, data);
    }
}
