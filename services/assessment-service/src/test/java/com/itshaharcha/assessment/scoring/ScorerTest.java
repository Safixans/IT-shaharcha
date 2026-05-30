package com.itshaharcha.assessment.scoring;

import com.itshaharcha.assessment.entity.Question;
import com.itshaharcha.assessment.entity.QuestionKind;
import com.itshaharcha.assessment.entity.Section;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ScorerTest {

    private final Scorer scorer = new Scorer();

    private Section section(UUID id, String name) {
        Section s = new Section();
        s.setId(id);
        s.setName(name);
        return s;
    }

    private Question question(UUID id, UUID sectionId, QuestionKind kind, double points, Object correct) {
        Question q = new Question();
        q.setId(id);
        q.setSectionId(sectionId);
        q.setKind(kind);
        q.setPoints(points);
        q.setCorrectAnswer(correct);
        return q;
    }

    @Test
    void scoresEarnedAndMaxWithPerSectionRollup() {
        UUID readingId = UUID.randomUUID();
        UUID writingId = UUID.randomUUID();
        UUID q1 = UUID.randomUUID();
        UUID q2 = UUID.randomUUID();
        UUID q3 = UUID.randomUUID();
        UUID essay = UUID.randomUUID();

        List<Section> sections = List.of(section(readingId, "Reading"), section(writingId, "Writing"));
        List<Question> questions = List.of(
                question(q1, readingId, QuestionKind.single_choice, 2, "B"),
                question(q2, readingId, QuestionKind.true_false, 1, true),
                question(q3, writingId, QuestionKind.multi_choice, 3, List.of("A", "C")),
                question(essay, writingId, QuestionKind.essay, 5, null));

        Map<UUID, Object> answers = Map.of(
                q1, "b",                       // case-insensitive match -> 2
                q2, "false",                   // wrong -> 0
                q3, List.of("C", "A"),         // set match (order-insensitive) -> 3
                essay, "an essay response");   // no correctAnswer -> 0 (manual)

        Scorer.ScoreResult result = scorer.score(questions, sections, answers);

        assertThat(result.scaledScore()).isEqualTo(5.0);
        assertThat(result.maxScore()).isEqualTo(11.0);
        assertThat(result.sectionScores())
                .extracting("section", "score")
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("Reading", 2.0),
                        org.assertj.core.groups.Tuple.tuple("Writing", 3.0));
    }

    @Test
    void unansweredQuestionsEarnZeroButStillCountTowardMax() {
        UUID sectionId = UUID.randomUUID();
        List<Section> sections = List.of(section(sectionId, "Reading"));
        List<Question> questions = List.of(
                question(UUID.randomUUID(), sectionId, QuestionKind.single_choice, 1, "A"));

        Scorer.ScoreResult result = scorer.score(questions, sections, Map.of());

        assertThat(result.scaledScore()).isZero();
        assertThat(result.maxScore()).isEqualTo(1.0);
    }
}
