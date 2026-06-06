package com.itshaharcha.assessment.dto.request;

import com.itshaharcha.assessment.entity.ProblemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ObjectiveQuestionInput(
        @NotNull ProblemType type,
        @NotBlank String prompt,
        String explanation,
        List<OptionInput> options,
        List<String> correctAnswers) {

    public record OptionInput(String text, boolean correct) {
    }
}
