package com.itshaharcha.assessment.scoring;

import com.itshaharcha.assessment.dto.SectionScore;
import com.itshaharcha.assessment.entity.Question;
import com.itshaharcha.assessment.entity.QuestionKind;
import com.itshaharcha.assessment.entity.Section;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Generic raw-points scorer. {@code maxScore} is the sum of every question's points;
 * {@code scaledScore} is the sum of points earned. Each correctly answered question
 * earns full points. Essays and any question without a {@code correctAnswer} earn 0
 * (left for manual grading). Per-section earned points are rolled up by section name.
 */
@Component
public class Scorer {

    public record ScoreResult(double scaledScore, Double maxScore, List<SectionScore> sectionScores) {
    }

    public ScoreResult score(List<Question> questions, List<Section> sections, Map<UUID, Object> answers) {
        Map<UUID, String> sectionNames = sections.stream()
                .collect(Collectors.toMap(Section::getId, Section::getName, (a, b) -> a, LinkedHashMap::new));

        Map<UUID, Double> earnedBySection = new LinkedHashMap<>();
        double totalEarned = 0;
        double totalMax = 0;
        for (Question q : questions) {
            double points = q.getPoints();
            totalMax += points;
            double earned = isCorrect(q, answers.get(q.getId())) ? points : 0;
            totalEarned += earned;
            earnedBySection.merge(q.getSectionId(), earned, Double::sum);
        }

        List<SectionScore> sectionScores = earnedBySection.entrySet().stream()
                .map(e -> new SectionScore(
                        sectionNames.getOrDefault(e.getKey(), e.getKey().toString()),
                        e.getValue()))
                .toList();

        return new ScoreResult(totalEarned, totalMax, sectionScores);
    }

    private boolean isCorrect(Question question, Object answer) {
        Object expected = question.getCorrectAnswer();
        if (expected == null || answer == null) {
            return false;
        }
        if (question.getKind() == QuestionKind.multi_choice) {
            return matchSet(expected, answer);
        }
        return normalize(expected).equals(normalize(answer));
    }

    private boolean matchSet(Object expected, Object answer) {
        Set<String> expectedSet = toSet(expected);
        Set<String> answerSet = toSet(answer);
        return !expectedSet.isEmpty() && expectedSet.equals(answerSet);
    }

    private Set<String> toSet(Object value) {
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(this::normalize).collect(Collectors.toSet());
        }
        return Set.of(normalize(value));
    }

    private String normalize(Object value) {
        return String.valueOf(value).trim().toLowerCase(Locale.ROOT);
    }
}
