package com.itshaharcha.analytics.entity;

import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Every accepted {@link com.itshaharcha.analytics.event.DomainEvent}, persisted for
 * idempotency (dedup on {@code eventId}) and to back the activity feed. The original
 * {@code data}/{@code roles} payloads are kept as JSON text so the event can be rebuilt.
 */
@Getter
@Setter
@Entity
@Table(name = "processed_events")
public class ProcessedEvent extends BaseEntity {

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String source;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "recorded_at")
    private Instant recordedAt;

    @Column(name = "subject_type")
    private String subjectType;

    @Column(name = "subject_id")
    private String subjectId;

    @Column(name = "roles_json", columnDefinition = "text")
    private String rolesJson;

    @Column(name = "data_json", columnDefinition = "text")
    private String dataJson;
}
