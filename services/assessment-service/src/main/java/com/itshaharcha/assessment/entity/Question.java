package com.itshaharcha.assessment.entity;

import com.itshaharcha.assessment.convert.ChoiceListConverter;
import com.itshaharcha.assessment.convert.JsonValueConverter;
import com.itshaharcha.assessment.dto.Choice;
import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "questions")
public class Question extends BaseEntity {

    @Column(name = "section_id", nullable = false)
    private UUID sectionId;

    @Column(name = "exam_id", nullable = false)
    private UUID examId;

    @Column(columnDefinition = "text", nullable = false)
    private String prompt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionKind kind;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(nullable = false)
    private double points = 1;

    @Convert(converter = ChoiceListConverter.class)
    @Column(columnDefinition = "text")
    private List<Choice> choices;

    /** Expected answer: a choice id, an array of choice ids, a boolean, or text. Null for essays. */
    @Convert(converter = JsonValueConverter.class)
    @Column(name = "correct_answer", columnDefinition = "text")
    private Object correctAnswer;

    @Column(columnDefinition = "text")
    private String explanation;
}
