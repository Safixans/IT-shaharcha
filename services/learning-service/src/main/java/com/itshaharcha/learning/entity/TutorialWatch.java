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
@Table(name = "tutorial_watches")
public class TutorialWatch extends BaseEntity {

    @Column(name = "tutorial_id", nullable = false)
    private UUID tutorialId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "watched_seconds", nullable = false)
    private int watchedSeconds;

    @Column(name = "position_seconds")
    private Integer positionSeconds;

    @Column(nullable = false)
    private boolean completed;
}
