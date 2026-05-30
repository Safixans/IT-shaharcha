package com.itshaharcha.assessment.entity;

import com.itshaharcha.assessment.convert.SectionScoreListConverter;
import com.itshaharcha.assessment.dto.SectionScore;
import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "exam_results")
public class ExamResult extends BaseEntity {

    @Column(name = "session_id", nullable = false, unique = true)
    private UUID sessionId;

    @Column(name = "exam_id", nullable = false)
    private UUID examId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false)
    private ExamType examType;

    @Column(name = "scaled_score", nullable = false)
    private double scaledScore;

    @Column(name = "max_score")
    private Double maxScore;

    @Convert(converter = SectionScoreListConverter.class)
    @Column(name = "section_scores", columnDefinition = "text")
    private List<SectionScore> sectionScores;

    @Column(name = "scored_at", nullable = false)
    private Instant scoredAt;

    @Column(columnDefinition = "text")
    private String feedback;
}
