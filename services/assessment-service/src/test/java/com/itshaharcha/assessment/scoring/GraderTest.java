package com.itshaharcha.assessment.scoring;

import com.itshaharcha.assessment.domain.AnswerRecord;
import com.itshaharcha.assessment.domain.StoredOption;
import com.itshaharcha.assessment.domain.StoredProblem;
import com.itshaharcha.assessment.entity.ProblemType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GraderTest {

    private final Grader grader = new Grader();

    @Test
    void input_normalizesWhitespaceAndCase() {
        UUID id = UUID.randomUUID();
        StoredProblem p = new StoredProblem(id, ProblemType.INPUT, null,
                List.of(new StoredOption(null, "Snails", true), new StoredOption(null, "a snail", true)), 1);

        GradeResult ok = grader.grade(List.of(p), Map.of(id, List.of("  snails ")));
        assertThat(ok.correct()).isEqualTo(1);
        assertThat(ok.total()).isEqualTo(1);

        GradeResult alt = grader.grade(List.of(p), Map.of(id, List.of("A   SNAIL")));
        assertThat(alt.correct()).isEqualTo(1);

        GradeResult wrong = grader.grade(List.of(p), Map.of(id, List.of("slugs")));
        assertThat(wrong.correct()).isZero();
        assertThat(wrong.incorrect()).isEqualTo(1);
    }

    @Test
    void radio_oneCorrectValue() {
        UUID id = UUID.randomUUID();
        StoredProblem p = new StoredProblem(id, ProblemType.RADIO, null,
                List.of(new StoredOption("1", "A", false), new StoredOption("2", "B", true)), 1);

        assertThat(grader.grade(List.of(p), Map.of(id, List.of("B"))).correct()).isEqualTo(1);
        assertThat(grader.grade(List.of(p), Map.of(id, List.of("A"))).correct()).isZero();
        assertThat(grader.grade(List.of(p), Map.of(id, List.of())).correct()).isZero();
    }

    @Test
    void multiSelect_poolMatchesEachCorrectValue() {
        UUID id = UUID.randomUUID();
        StoredProblem p = new StoredProblem(id, ProblemType.MULTI_SELECT, null,
                List.of(new StoredOption("1", "A", true),
                        new StoredOption("2", "B", false),
                        new StoredOption("3", "C", true)), 2);

        GradeResult perfect = grader.grade(List.of(p), Map.of(id, List.of("A", "C")));
        assertThat(perfect.total()).isEqualTo(2); // emits one record per correct slot
        assertThat(perfect.correct()).isEqualTo(2);

        GradeResult partial = grader.grade(List.of(p), Map.of(id, List.of("A", "B")));
        assertThat(partial.total()).isEqualTo(2);
        assertThat(partial.correct()).isEqualTo(1);
        assertThat(partial.incorrect()).isEqualTo(1);
    }

    @Test
    void unansweredProblemsCountAsIncorrect() {
        UUID id = UUID.randomUUID();
        StoredProblem p = new StoredProblem(id, ProblemType.INPUT, null,
                List.of(new StoredOption(null, "x", true)), 1);

        GradeResult gr = grader.grade(List.of(p), Map.of());
        assertThat(gr.total()).isEqualTo(1);
        assertThat(gr.correct()).isZero();
        assertThat(gr.answers()).extracting(AnswerRecord::correct).containsExactly(false);
    }
}
