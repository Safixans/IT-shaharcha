package com.itshaharcha.learning.entity;

import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lesson_progress")
public class LessonProgress extends BaseEntity {

    @Column(name = "lesson_id", nullable = false)
    private UUID lessonId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private boolean completed;

    @Column(name = "score_percent")
    private Double scorePercent;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
