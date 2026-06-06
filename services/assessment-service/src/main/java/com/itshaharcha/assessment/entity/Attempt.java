package com.itshaharcha.assessment.entity;

import com.itshaharcha.assessment.domain.AnswerRecord;
import com.itshaharcha.assessment.domain.StoredProblem;
import com.itshaharcha.assessment.domain.WritingCriteria;
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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * One student attempt of a unit. The unit's content + answer key are SNAPSHOTTED at start
 * so later edits/deactivation can't corrupt an in-flight (or already-graded) attempt.
 * Times are UTC {@link Instant}s — the API computes remainingSeconds server-side.
 */
@Getter
@Setter
@Entity
@Table(name = "attempts")
public class Attempt extends BaseEntity {

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "unit_id", nullable = false)
    private UUID unitId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private AttemptFamily family;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AttemptStatus status;

    @Column(nullable = false)
    private String title;

    // ---- snapshot of the unit at start ----
    @Column(name = "snapshot_section_data", columnDefinition = "text")
    private String snapshotSectionData;

    @Column(name = "snapshot_passage", columnDefinition = "text")
    private String snapshotPassage;

    @Column(name = "snapshot_prompt", columnDefinition = "text")
    private String snapshotPrompt;

    @Column(name = "snapshot_audio_id")
    private UUID snapshotAudioId;

    @Column(name = "snapshot_image_id")
    private UUID snapshotImageId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_problems", columnDefinition = "jsonb")
    private List<StoredProblem> snapshotProblems;

    // ---- in-progress draft (autosaved; graded on submit/expiry so a late submit still scores) ----
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "draft_answers", columnDefinition = "jsonb")
    private Map<String, List<String>> draftAnswers;

    @Column(name = "draft_essay", columnDefinition = "text")
    private String draftEssay;

    // ---- result ----
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<AnswerRecord> answers;

    @Column(columnDefinition = "text")
    private String essay;

    @Column(name = "correct_count", nullable = false)
    private int correctCount;

    @Column(name = "incorrect_count", nullable = false)
    private int incorrectCount;

    @Column(name = "total_count", nullable = false)
    private int totalCount;

    @Column(name = "score_percent")
    private Double scorePercent;

    private Double band;

    @Column(columnDefinition = "text")
    private String feedback;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private WritingCriteria criteria;

    @Column(name = "graded_by")
    private UUID gradedBy;

    // ---- timing (UTC) ----
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "graded_at")
    private Instant gradedAt;
}
