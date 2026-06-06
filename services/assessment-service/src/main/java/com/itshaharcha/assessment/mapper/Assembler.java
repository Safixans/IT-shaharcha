package com.itshaharcha.assessment.mapper;

import com.itshaharcha.assessment.domain.AnswerRecord;
import com.itshaharcha.assessment.domain.StoredProblem;
import com.itshaharcha.assessment.dto.response.AnswerReport;
import com.itshaharcha.assessment.dto.response.AttemptReport;
import com.itshaharcha.assessment.dto.response.AttemptSession;
import com.itshaharcha.assessment.dto.response.ServedProblem;
import com.itshaharcha.assessment.dto.response.Timing;
import com.itshaharcha.assessment.dto.response.UnitDetail;
import com.itshaharcha.assessment.dto.response.UnitMeta;
import com.itshaharcha.assessment.entity.Attempt;
import com.itshaharcha.assessment.entity.AttemptFamily;
import com.itshaharcha.assessment.entity.IeltsSkill;
import com.itshaharcha.assessment.entity.IeltsUnit;
import com.itshaharcha.assessment.entity.ObjectiveUnit;
import com.itshaharcha.assessment.entity.ProblemType;

import java.util.List;

/** Maps entities/snapshots → API DTOs, stripping correctness from anything served to students. */
public final class Assembler {

    private Assembler() {
    }

    public static AttemptFamily familyOf(IeltsSkill skill) {
        return switch (skill) {
            case LISTENING -> AttemptFamily.IELTS_LISTENING;
            case READING -> AttemptFamily.IELTS_READING;
            case WRITING -> AttemptFamily.IELTS_WRITING;
        };
    }

    public static UnitMeta meta(IeltsUnit u) {
        return new UnitMeta(u.getId(), familyOf(u.getSkill()), u.getTitle(), u.getTags(),
                u.isActive(), u.getProblemCount(), u.getDurationSeconds(), null, u.getWritingTask());
    }

    public static UnitMeta meta(ObjectiveUnit u) {
        AttemptFamily family = u.getKind() == com.itshaharcha.assessment.entity.UnitKind.SAT
                ? AttemptFamily.SAT : AttemptFamily.QUIZ;
        return new UnitMeta(u.getId(), family, u.getTitle(), u.getTags(),
                u.isActive(), u.getProblemCount(), u.getDurationSeconds(), u.getSatSection(), null);
    }

    /**
     * @param includeAnswers when true (authors only), exposes {@code originalSectionData} — the
     *                        authored HTML with answer markers — so the editor can round-trip it.
     */
    public static UnitDetail detail(IeltsUnit u, boolean includeAnswers) {
        AttemptFamily family = familyOf(u.getSkill());
        return new UnitDetail(u.getId(), family, u.getTitle(), u.getTags(), u.isActive(),
                u.getProblemCount(), u.getDurationSeconds(), null, u.getWritingTask(),
                u.getSectionData(), includeAnswers ? u.getOriginalSectionData() : null,
                u.getPassage(), u.getPrompt(), u.getAudioId(), u.getImageId(),
                served(family, u.getProblems()));
    }

    public static UnitDetail detail(ObjectiveUnit u) {
        UnitMeta m = meta(u);
        return new UnitDetail(u.getId(), m.family(), u.getTitle(), u.getTags(), u.isActive(),
                u.getProblemCount(), u.getDurationSeconds(), u.getSatSection(), null,
                null, null, null, null, null, null, served(m.family(), u.getQuestions()));
    }

    /** Served problems with correctness withheld. IELTS L/R carry their widgets in the HTML. */
    public static List<ServedProblem> served(AttemptFamily family, List<StoredProblem> problems) {
        if (problems == null) {
            return List.of();
        }
        boolean objective = family == AttemptFamily.SAT || family == AttemptFamily.QUIZ;
        return problems.stream().map(p -> {
            if (!objective) {
                return new ServedProblem(p.problemId(), p.type(), null, null);
            }
            // INPUT answers must never be served as options.
            List<ServedProblem.Option> opts = p.type() == ProblemType.INPUT || p.options() == null
                    ? null
                    : p.options().stream().map(o -> new ServedProblem.Option(o.id(), o.text())).toList();
            return new ServedProblem(p.problemId(), p.type(), p.prompt(), opts);
        }).toList();
    }

    public static AttemptSession session(Attempt a) {
        return new AttemptSession(a.getId(), a.getUnitId(), a.getFamily(), a.getStatus(), a.getTitle(),
                Timing.of(a.getStartedAt(), a.getEndsAt()),
                a.getSnapshotSectionData(), a.getSnapshotPassage(), a.getSnapshotPrompt(),
                a.getSnapshotAudioId(), a.getSnapshotImageId(),
                served(a.getFamily(), a.getSnapshotProblems()));
    }

    public static AttemptReport report(Attempt a) {
        List<AnswerReport> answers = a.getAnswers() == null ? null
                : a.getAnswers().stream()
                .map(r -> new AnswerReport(r.problemId(), r.submitted(), r.correctOptions(), r.correct()))
                .toList();
        return new AttemptReport(a.getId(), a.getUnitId(), a.getStudentId(), a.getFamily(),
                a.getTitle(), a.getStatus(), a.getCorrectCount(), a.getIncorrectCount(), a.getTotalCount(),
                a.getScorePercent(), a.getBand(), answers, a.getEssay(), a.getFeedback(), a.getCriteria(),
                a.getStartedAt(), a.getSubmittedAt(), a.getGradedAt());
    }
}
