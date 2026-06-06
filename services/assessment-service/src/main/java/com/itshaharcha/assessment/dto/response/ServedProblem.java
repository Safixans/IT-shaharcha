package com.itshaharcha.assessment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.assessment.entity.ProblemType;

import java.util.List;
import java.util.UUID;

/** A problem as served to a candidate — correctness withheld. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ServedProblem(
        UUID problemId,
        ProblemType type,
        String prompt,
        List<Option> options) {

    public record Option(String id, String text) {
    }
}
