package com.itshaharcha.assessment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.assessment.dto.Choice;
import com.itshaharcha.assessment.entity.QuestionKind;

import java.util.List;
import java.util.UUID;

/** A question as presented during a live session — correct answer withheld (spec SessionQuestion). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SessionQuestionResponse(
        UUID id,
        UUID sectionId,
        String prompt,
        QuestionKind kind,
        int order,
        double points,
        List<Choice> choices) {
}
