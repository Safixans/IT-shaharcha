package com.itshaharcha.assessment.domain;

import com.itshaharcha.assessment.entity.ProblemType;

import java.util.List;
import java.util.UUID;

/**
 * One stored problem (the immutable answer key). Used in both the IELTS unit jsonb and the
 * objective unit jsonb. {@code prompt} is set for objective questions (IELTS prompts live in
 * the HTML). {@code options} carries the choices (for INPUT, the acceptable answers as correct
 * options). The correct values are derived from {@code options} where {@code correct} is true.
 */
public record StoredProblem(
        UUID problemId,
        ProblemType type,
        String prompt,
        List<StoredOption> options,
        int correctCount) {

    public List<String> correctValues() {
        return options == null ? List.of()
                : options.stream().filter(StoredOption::correct).map(StoredOption::text).toList();
    }
}
