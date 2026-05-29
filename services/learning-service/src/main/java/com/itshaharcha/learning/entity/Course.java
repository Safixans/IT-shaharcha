package com.itshaharcha.learning.entity;

import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "courses")
public class Course extends BaseEntity {

    @Column(name = "track_id")
    private UUID trackId;

    @Column(nullable = false)
    private String title;

    private String slug;

    @Column(columnDefinition = "text")
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Level level = Level.beginner;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;
}
