package com.itshaharcha.assessment.dto.request;

import java.util.List;

/** Objective families send {@code answers}; Writing sends {@code essay}. Either may be null. */
public record AttemptSubmit(
        List<AnswerInput> answers,
        String essay) {
}
