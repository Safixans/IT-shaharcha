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
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "source_sync_runs")
public class SourceSyncRun extends BaseEntity {

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncStatus status = SyncStatus.queued;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "items_imported")
    private Integer itemsImported;
}
