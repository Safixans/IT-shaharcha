package com.itshaharcha.assessment.scoring;

import com.itshaharcha.assessment.domain.AnswerRecord;
import com.itshaharcha.assessment.domain.StoredProblem;
import com.itshaharcha.assessment.entity.ProblemType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Value-based grader (position/orderIndex irrelevant). Matching is normalized: trimmed,
 * case-insensitive, internal whitespace collapsed. MULTI_SELECT is pool-matched by value —
 * each correct option that appears in the submission scores; surplus wrong picks are consumed.
 */
@Component
public class Grader {

    public GradeResult grade(List<StoredProblem> key, Map<UUID, List<String>> submitted) {
        List<AnswerRecord> answers = new ArrayList<>();
        for (StoredProblem p : key == null ? List.<StoredProblem>of() : key) {
            List<String> values = submitted.getOrDefault(p.problemId(), List.of());
            if (p.type() == ProblemType.MULTI_SELECT) {
                answers.addAll(gradeMulti(p, values));
            } else {
                answers.add(gradeSingle(p, values));
            }
        }
        int correct = (int) answers.stream().filter(AnswerRecord::correct).count();
        return new GradeResult(answers, correct, answers.size() - correct, answers.size());
    }

    private AnswerRecord gradeSingle(StoredProblem p, List<String> values) {
        List<String> correctOptions = p.correctValues();
        String submitted = values.stream().findFirst().map(String::trim).orElse("");
        boolean correct = correctOptions.stream().anyMatch(c -> eq(c, submitted));
        return new AnswerRecord(p.problemId(), List.of(submitted), correctOptions, correct);
    }

    private List<AnswerRecord> gradeMulti(StoredProblem p, List<String> values) {
        List<String> correctOptions = p.correctValues();
        List<String> pool = new ArrayList<>(values.stream().map(String::trim).filter(s -> !s.isEmpty()).toList());
        List<AnswerRecord> out = new ArrayList<>();
        for (String correctOpt : correctOptions) {
            String match = pool.stream().filter(s -> eq(s, correctOpt)).findFirst().orElse(null);
            if (match != null) {
                pool.remove(match);
                out.add(new AnswerRecord(p.problemId(), List.of(match), List.of(correctOpt), true));
            } else {
                // consume a wrong pick (so over-selecting can't sneak a free correct slot)
                String wrong = pool.stream()
                        .filter(s -> correctOptions.stream().noneMatch(c -> eq(c, s)))
                        .findFirst().orElse("");
                if (!wrong.isEmpty()) {
                    pool.remove(wrong);
                }
                out.add(new AnswerRecord(p.problemId(),
                        wrong.isEmpty() ? List.of() : List.of(wrong), List.of(correctOpt), false));
            }
        }
        return out;
    }

    private boolean eq(String a, String b) {
        return normalize(a).equals(normalize(b));
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
