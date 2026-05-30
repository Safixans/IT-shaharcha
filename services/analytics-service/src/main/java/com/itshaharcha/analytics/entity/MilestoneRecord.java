package com.itshaharcha.analytics.entity;

import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/** A milestone reached by an account, derived from the event stream. One per (account, milestone). */
@Getter
@Setter
@Entity
@Table(name = "milestones",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_milestones_account_milestone",
                columnNames = {"account_id", "milestone"}))
public class MilestoneRecord extends BaseEntity {

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private String milestone;

    @Column(nullable = false)
    private int points = 0;

    private String domain;

    @Column(name = "reached_at", nullable = false)
    private Instant reachedAt;
}
