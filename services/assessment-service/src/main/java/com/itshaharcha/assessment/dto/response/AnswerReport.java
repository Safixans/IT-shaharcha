package com.itshaharcha.assessment.dto.response;

import java.util.List;
import java.util.UUID;

public record AnswerReport(
        UUID problemId,
        List<String> submitted,
        List<String> correctOptions,
        boolean correct) {
}
