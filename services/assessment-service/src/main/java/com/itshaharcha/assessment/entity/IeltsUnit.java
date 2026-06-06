package com.itshaharcha.assessment.entity;

import com.itshaharcha.assessment.domain.StoredProblem;
import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A single-skill IELTS training unit. Content is authored as HTML; the server parses it once
 * into the immutable {@code problems} answer key and the answer-stripped {@code sectionData}
 * served to students. Editing re-parses and overwrites (delete+recreate semantics).
 */
@Getter
@Setter
@Entity
@Table(name = "ielts_units")
public class IeltsUnit extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private IeltsSkill skill;

    @Column(nullable = false)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> tags = new ArrayList<>();

    @Column(name = "original_section_data", columnDefinition = "text")
    private String originalSectionData;

    @Column(name = "section_data", columnDefinition = "text")
    private String sectionData;

    @Column(columnDefinition = "text")
    private String passage;

    @Column(columnDefinition = "text")
    private String prompt;

    @Enumerated(EnumType.STRING)
    @Column(name = "writing_task", length = 16)
    private WritingTask writingTask;

    /** Immutable answer key (null for Writing). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<StoredProblem> problems;

    @Column(name = "problem_count", nullable = false)
    private int problemCount;

    @Column(name = "audio_id")
    private UUID audioId;

    @Column(name = "image_id")
    private UUID imageId;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(nullable = false)
    private boolean active = false;
}
