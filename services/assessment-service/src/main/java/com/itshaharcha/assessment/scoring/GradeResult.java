package com.itshaharcha.assessment.scoring;

import com.itshaharcha.assessment.domain.AnswerRecord;

import java.util.List;

public record GradeResult(List<AnswerRecord> answers, int correct, int incorrect, int total) {
}
