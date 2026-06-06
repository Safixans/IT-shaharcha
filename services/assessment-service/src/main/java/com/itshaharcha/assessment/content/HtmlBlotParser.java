package com.itshaharcha.assessment.content;

import com.itshaharcha.assessment.domain.StoredOption;
import com.itshaharcha.assessment.domain.StoredProblem;
import com.itshaharcha.assessment.entity.ProblemType;
import com.itshaharcha.common.exception.ApplicationException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Parses authored IELTS HTML (Quill "blots") into the immutable answer key and produces the
 * answer-stripped HTML served to students. Recognized blots:
 *   <input type=text value="a / b">           → INPUT  (acceptable answers, '/'-delimited)
 *   <select-blot data-correct-option="X">      → SELECT (one correct)
 *   <radio-blot  data-correct-option="X">      → RADIO  (one correct)
 *   <checkbox-blot data-correct-options='[..]'>→ MULTI_SELECT (N correct, configurable)
 * Each blot is tagged with data-problem-id; all correctness markers are removed from the output.
 */
@Component
public class HtmlBlotParser {

    public ParsedContent parse(String html) {
        if (html == null || html.isBlank()) {
            throw ApplicationException.badRequest("Content is empty");
        }
        Document doc = Jsoup.parseBodyFragment(html);
        List<StoredProblem> problems = new ArrayList<>();
        int marks = 0;
        for (Element el : doc.select("input[type=text], select-blot, radio-blot, checkbox-blot")) {
            StoredProblem p = parseProblem(el);
            problems.add(p);
            marks += (p.type() == ProblemType.MULTI_SELECT) ? p.correctCount() : 1;
        }
        return new ParsedContent(doc.body().html(), problems, marks);
    }

    private StoredProblem parseProblem(Element el) {
        UUID id = UUID.randomUUID();
        ProblemType type = switch (el.tagName()) {
            case "select-blot" -> ProblemType.SELECT;
            case "radio-blot" -> ProblemType.RADIO;
            case "checkbox-blot" -> ProblemType.MULTI_SELECT;
            default -> ProblemType.INPUT;
        };
        List<StoredOption> options;
        int correctCount = 1;
        switch (type) {
            case INPUT -> {
                String value = el.attr("value").trim();
                options = Arrays.stream(value.split("/"))
                        .map(String::trim).filter(s -> !s.isEmpty())
                        .map(s -> new StoredOption(null, s, true))
                        .toList();
                el.removeAttr("value");
            }
            case SELECT -> {
                String correct = el.attr("data-correct-option").trim();
                options = el.select("option").stream()
                        .map(o -> new StoredOption(o.attr("name"), o.attr("value").trim(),
                                o.attr("value").trim().equals(correct)))
                        .toList();
                el.removeAttr("data-correct-option");
            }
            case RADIO -> {
                String correct = el.attr("data-correct-option").trim();
                options = el.select("input[type=radio]").stream()
                        .map(i -> new StoredOption(i.attr("name"), i.attr("value").trim(),
                                i.attr("value").trim().equals(correct)))
                        .toList();
                el.removeAttr("data-correct-option");
            }
            default -> { // MULTI_SELECT
                List<String> correct = parseJsonArray(el.attr("data-correct-options"));
                correctCount = correct.isEmpty() ? 1 : correct.size();
                options = el.select("input[type=checkbox]").stream()
                        .map(i -> new StoredOption(i.attr("name"), i.attr("value").trim(),
                                correct.contains(i.attr("value").trim())))
                        .toList();
                el.removeAttr("data-correct-options");
                el.removeAttr("correctcount");
            }
        }
        if (options.isEmpty() || options.stream().noneMatch(StoredOption::correct)) {
            throw ApplicationException.badRequest("A question has no correct answer defined");
        }
        // Tag the served element so the client can map widgets → problemId, then strip markers.
        el.attr("data-problem-id", id.toString());
        el.select("[checked]").removeAttr("checked");
        el.select("[selected]").removeAttr("selected");
        return new StoredProblem(id, type, null, options, correctCount);
    }

    /** Parse a JSON array attribute like ["HEY","Hi"] (handles &quot; entities). */
    private List<String> parseJsonArray(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String cleaned = raw.replace("[", "").replace("]", "")
                .replace("\"", "").replace("&quot;", "");
        return Arrays.stream(cleaned.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
