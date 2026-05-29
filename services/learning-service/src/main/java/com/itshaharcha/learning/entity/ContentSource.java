package com.itshaharcha.learning.entity;

import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "content_sources")
public class ContentSource extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceTarget target;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private boolean enabled = true;

    private String schedule;

    @Column(name = "default_topic")
    private String defaultTopic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceStatus status = SourceStatus.active;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    @Column(name = "item_count", nullable = false)
    private int itemCount;
}
