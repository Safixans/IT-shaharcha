package com.itshaharcha.assessment.entity;

import com.itshaharcha.assessment.convert.JsonValueConverter;
import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "session_answers")
public class SessionAnswer extends BaseEntity {

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "section_id")
    private UUID sectionId;

    /** Candidate's answer payload — choice id(s), boolean, or text. */
    @Convert(converter = JsonValueConverter.class)
    @Column(columnDefinition = "text")
    private Object value;
}
