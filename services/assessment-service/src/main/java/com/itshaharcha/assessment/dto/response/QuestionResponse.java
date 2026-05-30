package com.itshaharcha.assessment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.assessment.dto.Choice;
import com.itshaharcha.assessment.entity.QuestionKind;

import java.util.List;
import java.util.UUID;

/** Full question including the correct answer — admin authoring only (spec Question). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record QuestionResponse(
        UUID id,
        UUID sectionId,
        String prompt,
        QuestionKind kind,
        int order,
        double points,
        List<Choice> choices,
        Object correctAnswer,
        String explanation) {
}
