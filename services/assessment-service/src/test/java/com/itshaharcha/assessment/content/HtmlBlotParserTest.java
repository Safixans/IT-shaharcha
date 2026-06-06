package com.itshaharcha.assessment.content;

import com.itshaharcha.assessment.domain.StoredProblem;
import com.itshaharcha.assessment.entity.ProblemType;
import com.itshaharcha.common.exception.ApplicationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HtmlBlotParserTest {

    private final HtmlBlotParser parser = new HtmlBlotParser();

    @Test
    void input_acceptsSlashDelimitedAlternatives_andStripsValue() {
        ParsedContent pc = parser.parse("<p>The animal is <input type=\"text\" value=\"snails / a snail\"></p>");

        assertThat(pc.problems()).hasSize(1);
        StoredProblem p = pc.problems().get(0);
        assertThat(p.type()).isEqualTo(ProblemType.INPUT);
        assertThat(p.correctValues()).containsExactly("snails", "a snail");
        assertThat(pc.problemCount()).isEqualTo(1);
        assertThat(pc.sectionData()).doesNotContain("value=");
        assertThat(pc.sectionData()).contains("data-problem-id");
    }

    @Test
    void select_marksOneCorrect_andStripsMarker() {
        ParsedContent pc = parser.parse("""
                <select-blot data-correct-option="B">
                  <option name="o1" value="A"></option>
                  <option name="o2" value="B"></option>
                </select-blot>
                """);

        StoredProblem p = pc.problems().get(0);
        assertThat(p.type()).isEqualTo(ProblemType.SELECT);
        assertThat(p.correctValues()).containsExactly("B");
        assertThat(pc.problemCount()).isEqualTo(1);
        assertThat(pc.sectionData()).doesNotContain("data-correct-option");
    }

    @Test
    void radio_marksOneCorrect() {
        ParsedContent pc = parser.parse("""
                <radio-blot data-correct-option="yes">
                  <input type="radio" name="r1" value="yes"/>
                  <input type="radio" name="r2" value="no"/>
                </radio-blot>
                """);

        StoredProblem p = pc.problems().get(0);
        assertThat(p.type()).isEqualTo(ProblemType.RADIO);
        assertThat(p.correctValues()).containsExactly("yes");
    }

    @Test
    void multiSelect_countsCorrectAsCorrectCount() {
        ParsedContent pc = parser.parse("""
                <checkbox-blot data-correct-options='["A","C"]'>
                  <input type="checkbox" name="c1" value="A"/>
                  <input type="checkbox" name="c2" value="B"/>
                  <input type="checkbox" name="c3" value="C"/>
                </checkbox-blot>
                """);

        StoredProblem p = pc.problems().get(0);
        assertThat(p.type()).isEqualTo(ProblemType.MULTI_SELECT);
        assertThat(p.correctCount()).isEqualTo(2);
        assertThat(p.correctValues()).containsExactlyInAnyOrder("A", "C");
        // MULTI_SELECT contributes correctCount marks to the total.
        assertThat(pc.problemCount()).isEqualTo(2);
    }

    @Test
    void multipleBlots_sumProblemCount() {
        ParsedContent pc = parser.parse(
                "<input type=\"text\" value=\"x\"> and <input type=\"text\" value=\"y\">");
        assertThat(pc.problems()).hasSize(2);
        assertThat(pc.problemCount()).isEqualTo(2);
    }

    @Test
    void rejectsBlotWithNoCorrectAnswer() {
        assertThatThrownBy(() -> parser.parse("<input type=\"text\" value=\"\">"))
                .isInstanceOf(ApplicationException.class);
    }

    @Test
    void rejectsEmptyContent() {
        assertThatThrownBy(() -> parser.parse("   ")).isInstanceOf(ApplicationException.class);
    }
}
