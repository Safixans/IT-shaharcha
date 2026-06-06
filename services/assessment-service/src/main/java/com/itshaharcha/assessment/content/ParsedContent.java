package com.itshaharcha.assessment.content;

import com.itshaharcha.assessment.domain.StoredProblem;

import java.util.List;

/**
 * Result of parsing authored HTML: the answer-stripped HTML to serve, the immutable answer
 * key, and the marks count (MULTI_SELECT contributes its correctCount; everything else 1).
 */
public record ParsedContent(String sectionData, List<StoredProblem> problems, int problemCount) {
}
