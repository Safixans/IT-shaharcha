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
@Table(name = "docs")
public class Doc extends BaseEntity {

    @Column(nullable = false)
    private String title;

    private String topic;

    private String url;

    @Column(columnDefinition = "text")
    private String body;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(name = "source_id")
    private UUID sourceId;
}
