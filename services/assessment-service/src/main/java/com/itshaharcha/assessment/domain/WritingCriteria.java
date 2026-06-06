package com.itshaharcha.assessment.domain;

/** IELTS Writing band criteria (each 0–9, half bands). Null until the teacher grades. */
public record WritingCriteria(
        Double taskAchievement,
        Double coherenceCohesion,
        Double lexicalResource,
        Double grammaticalRange) {
}
