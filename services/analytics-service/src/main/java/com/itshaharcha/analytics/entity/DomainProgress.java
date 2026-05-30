package com.itshaharcha.analytics.entity;

import com.itshaharcha.analytics.convert.IntMapConverter;
import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The folded per-account, per-domain roll-up maintained from the event stream:
 * cumulative points, headline counters, last activity, and (optional) level/streak.
 * One row per (accountId, domain).
 */
@Getter
@Setter
@Entity
@Table(name = "domain_progress",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_domain_progress_account_domain",
                columnNames = {"account_id", "domain"}))
public class DomainProgress extends BaseEntity {

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private String domain;

    @Column(nullable = false)
    private long points = 0;

    private Integer level;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @Convert(converter = IntMapConverter.class)
    @Column(columnDefinition = "text", nullable = false)
    private Map<String, Integer> counters = new LinkedHashMap<>();

    @Column(name = "streak_days")
    private Integer streakDays;
}
