package com.itshaharcha.analytics.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Canonical platform event envelope (shared/events.yaml). Analytics CONSUMES these from
 * every {@code itsh.<service>.events} topic (and via REST ingest), and emits its own
 * {@code analytics.*} events to {@code itsh.analytics.events}.
 *
 * <p>Unknown envelope fields (e.g. evolving {@code context}) are ignored so a newer
 * producer never breaks ingestion.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record DomainEvent(
        UUID eventId,
        String type,
        String source,
        String specVersion,
        Instant occurredAt,
        Instant recordedAt,
        Actor actor,
        Subject subject,
        Map<String, Object> context,
        Map<String, Object> data) {

    public static final String TOPIC = "itsh.analytics.events";
    public static final String SOURCE = "analytics";
    public static final String SPEC_VERSION = "1.0";

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Actor(UUID accountId, Set<String> roles, Boolean anonymous) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Subject(String type, String id) {
    }

    /** Build an analytics-sourced event (e.g. milestone.reached). */
    public static DomainEvent analytics(String type, UUID accountId, Set<String> roles,
                                        Subject subject, Map<String, Object> data) {
        return new DomainEvent(UUID.randomUUID(), type, SOURCE, SPEC_VERSION,
                Instant.now(), Instant.now(), new Actor(accountId, roles, false),
                subject, null, data);
    }
}
