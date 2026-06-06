package com.itshaharcha.assessment.domain;

import java.util.List;
import java.util.UUID;

/** The graded outcome for one problem, stored on the attempt. */
public record AnswerRecord(
        UUID problemId,
        List<String> submitted,
        List<String> correctOptions,
        boolean correct) {
}
