package com.itshaharcha.assessment.entity;

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
@Table(name = "sections")
public class Section extends BaseEntity {

    @Column(name = "exam_id", nullable = false)
    private UUID examId;

    @Column(nullable = false)
    private String name;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(columnDefinition = "text")
    private String content;

    @Column(name = "pdf_url", columnDefinition = "text")
    private String pdfUrl;
}
