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
@Table(name = "typing_sessions")
public class TypingSession extends BaseEntity {

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "lesson_id")
    private UUID lessonId;

    @Column(nullable = false)
    private double wpm;

    @Column(name = "accuracy_percent", nullable = false)
    private double accuracyPercent;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    private Integer keystrokes;
}
