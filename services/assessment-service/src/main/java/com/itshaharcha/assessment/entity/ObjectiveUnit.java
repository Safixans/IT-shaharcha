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

/** A SAT module or generic quiz: a flat list of objective questions with an answer key. */
@Getter
@Setter
@Entity
@Table(name = "objective_units")
public class ObjectiveUnit extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UnitKind kind;

    @Enumerated(EnumType.STRING)
    @Column(name = "sat_section", length = 24)
    private SatSection satSection;

    @Column(nullable = false)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> tags = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<StoredProblem> questions = new ArrayList<>();

    @Column(name = "problem_count", nullable = false)
    private int problemCount;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(nullable = false)
    private boolean active = false;
}
