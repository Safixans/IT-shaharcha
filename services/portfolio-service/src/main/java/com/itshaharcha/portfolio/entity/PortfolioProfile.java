package com.itshaharcha.portfolio.entity;

import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Per-account publish state for the assembled academic portfolio. The portfolio's
 * content (certificates, education, items) lives in their own tables and is gathered
 * on read; this row only holds the public handle, visibility, and publish timestamp.
 */
@Getter
@Setter
@Entity
@Table(name = "portfolios")
public class PortfolioProfile extends BaseEntity {

    @Column(name = "account_id", nullable = false, unique = true)
    private UUID accountId;

    @Column(unique = true)
    private String handle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility = Visibility.PRIVATE;

    @Column(name = "published_at")
    private Instant publishedAt;
}
