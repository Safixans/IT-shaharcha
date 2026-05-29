package com.itshaharcha.learning.entity;

import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "doc_reads")
public class DocRead extends BaseEntity {

    @Column(name = "doc_id", nullable = false)
    private UUID docId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "scroll_percent")
    private Double scrollPercent;
}
