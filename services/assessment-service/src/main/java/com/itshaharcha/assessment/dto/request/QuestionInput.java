package com.itshaharcha.assessment.dto.request;

import com.itshaharcha.assessment.dto.Choice;
import com.itshaharcha.assessment.entity.QuestionKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record QuestionInput(
        @NotBlank String prompt,
        @NotNull QuestionKind kind,
        @PositiveOrZero Integer order,
        @PositiveOrZero Double points,
        List<Choice> choices,
        Object correctAnswer,
        String explanation) {
}
