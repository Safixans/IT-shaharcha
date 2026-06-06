package com.itshaharcha.assessment.service;

import com.itshaharcha.assessment.domain.StoredProblem;
import com.itshaharcha.assessment.dto.request.AnswerInput;
import com.itshaharcha.assessment.dto.request.AttemptSubmit;
import com.itshaharcha.assessment.dto.response.AttemptReport;
import com.itshaharcha.assessment.dto.response.AttemptSession;
import com.itshaharcha.assessment.dto.response.PageResponse;
import com.itshaharcha.assessment.entity.Attempt;
import com.itshaharcha.assessment.entity.AttemptFamily;
import com.itshaharcha.assessment.entity.AttemptStatus;
import com.itshaharcha.assessment.entity.IeltsUnit;
import com.itshaharcha.assessment.entity.ObjectiveUnit;
import com.itshaharcha.assessment.entity.UnitKind;
import com.itshaharcha.assessment.event.AssessmentEventPublisher;
import com.itshaharcha.assessment.mapper.Assembler;
import com.itshaharcha.assessment.repository.AttemptRepository;
import com.itshaharcha.assessment.repository.IeltsUnitRepository;
import com.itshaharcha.assessment.repository.ObjectiveUnitRepository;
import com.itshaharcha.assessment.scoring.BandTable;
import com.itshaharcha.assessment.scoring.GradeResult;
import com.itshaharcha.assessment.scoring.Grader;
import com.itshaharcha.assessment.security.SecurityUtils;
import com.itshaharcha.common.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Attempt lifecycle: start (with snapshot + per-family resume), autosave, submit, and history.
 * Times are UTC; resume/start responses carry server-computed remainingSeconds so the client
 * never diffs its own clock against a server timestamp. A unit's content + answer key are
 * snapshotted at start, so later edits/deactivation can't corrupt a running or finished attempt.
 */
@Service
@RequiredArgsConstructor
public class AttemptService {

    private static final Set<AttemptStatus> TERMINAL = Set.of(
            AttemptStatus.COMPLETED, AttemptStatus.PENDING_GRADING,
            AttemptStatus.GRADED, AttemptStatus.EXPIRED);

    private final AttemptRepository attempts;
    private final IeltsUnitRepository ieltsUnits;
    private final ObjectiveUnitRepository objectiveUnits;
    private final Grader grader;
    private final AssessmentEventPublisher events;

    @Transactional
    public AttemptSession start(UUID unitId) {
        UUID student = SecurityUtils.currentAccountId();
        StartSpec spec = resolve(unitId);
        if (!spec.active()) {
            throw ApplicationException.badRequest("Unit is not available");
        }

        var existing = attempts.findFirstByStudentIdAndFamilyAndStatusOrderByStartedAtDesc(
                student, spec.family(), AttemptStatus.IN_PROGRESS);
        if (existing.isPresent()) {
            Attempt running = existing.get();
            if (running.getUnitId().equals(unitId) && Instant.now().isBefore(running.getEndsAt())) {
                return Assembler.session(running); // same active unit → resume, not a new attempt
            }
            finalizeExpired(running); // abandoned/timed-out, or switching to another unit
        }

        return Assembler.session(create(student, unitId, spec));
    }

    private Attempt create(UUID student, UUID unitId, StartSpec spec) {
        Instant now = Instant.now();
        Attempt a = new Attempt();
        a.setStudentId(student);
        a.setUnitId(unitId);
        a.setFamily(spec.family());
        a.setStatus(AttemptStatus.IN_PROGRESS);
        a.setTitle(spec.title());
        a.setSnapshotSectionData(spec.sectionData());
        a.setSnapshotPassage(spec.passage());
        a.setSnapshotPrompt(spec.prompt());
        a.setSnapshotAudioId(spec.audioId());
        a.setSnapshotImageId(spec.imageId());
        a.setSnapshotProblems(spec.problems());
        a.setStartedAt(now);
        a.setEndsAt(now.plusSeconds(spec.duration()));
        Attempt saved = attempts.save(a);
        events.emit("assessment.attempt.started", "attempt", saved.getId(),
                Map.of("family", spec.family().name(), "unitId", unitId.toString()));
        return saved;
    }

    @Transactional
    public void autosave(UUID attemptId, AttemptSubmit body) {
        Attempt a = owned(attemptId);
        if (a.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw ApplicationException.conflict("Attempt is not in progress");
        }
        if (Instant.now().isAfter(a.getEndsAt())) {
            finalizeExpired(a); // time's up — keep whatever was already saved
            return;
        }
        a.setDraftAnswers(toStringMap(body.answers()));
        a.setDraftEssay(body.essay());
        attempts.save(a);
    }

    @Transactional
    public AttemptReport submit(UUID attemptId, AttemptSubmit body) {
        Attempt a = owned(attemptId);
        if (TERMINAL.contains(a.getStatus())) {
            return Assembler.report(a); // idempotent once completed
        }
        boolean late = Instant.now().isAfter(a.getEndsAt());
        Map<UUID, List<String>> answers = late ? fromStringMap(a.getDraftAnswers()) : toUuidMap(body.answers());
        String essay = late ? a.getDraftEssay() : (body == null ? null : body.essay());

        if (a.getFamily() == AttemptFamily.IELTS_WRITING) {
            a.setEssay(essay);
            a.setStatus(AttemptStatus.PENDING_GRADING);
        } else {
            score(a, answers);
            a.setStatus(AttemptStatus.COMPLETED);
        }
        a.setSubmittedAt(Instant.now());
        a.setDraftAnswers(null);
        a.setDraftEssay(null);
        Attempt saved = attempts.save(a);
        events.emit("assessment.attempt.submitted", "attempt", saved.getId(),
                Map.of("family", saved.getFamily().name(), "status", saved.getStatus().name(),
                        "scorePercent", saved.getScorePercent() == null ? -1.0 : saved.getScorePercent()));
        return Assembler.report(saved);
    }

    @Transactional(readOnly = true)
    public AttemptReport get(UUID attemptId) {
        return Assembler.report(owned(attemptId));
    }

    @Transactional(readOnly = true)
    public PageResponse<AttemptReport> myAttempts(AttemptFamily family, Pageable pageable) {
        UUID student = SecurityUtils.currentAccountId();
        Page<Attempt> page = family == null
                ? attempts.findByStudentIdOrderByStartedAtDesc(student, pageable)
                : attempts.findByStudentIdAndFamilyOrderByStartedAtDesc(student, family, pageable);
        return PageResponse.from(page, Assembler::report);
    }

    // ---- grading-side support (used by GradingService) ----

    Attempt loadForGrading(UUID attemptId) {
        return attempts.findById(attemptId)
                .orElseThrow(() -> ApplicationException.notFound("Attempt not found"));
    }

    // ---- internals ----

    private void score(Attempt a, Map<UUID, List<String>> answers) {
        GradeResult gr = grader.grade(a.getSnapshotProblems(), answers);
        a.setAnswers(gr.answers());
        a.setCorrectCount(gr.correct());
        a.setIncorrectCount(gr.incorrect());
        a.setTotalCount(gr.total());
        a.setScorePercent(gr.total() == 0 ? 0.0 : round1(100.0 * gr.correct() / gr.total()));
        if (a.getFamily() == AttemptFamily.IELTS_LISTENING) {
            a.setBand(BandTable.listening(gr.correct()));
        }
    }

    private void finalizeExpired(Attempt a) {
        if (a.getFamily() == AttemptFamily.IELTS_WRITING) {
            a.setEssay(a.getDraftEssay());
            boolean hasEssay = a.getDraftEssay() != null && !a.getDraftEssay().isBlank();
            a.setStatus(hasEssay ? AttemptStatus.PENDING_GRADING : AttemptStatus.EXPIRED);
        } else {
            score(a, fromStringMap(a.getDraftAnswers()));
            a.setStatus(AttemptStatus.EXPIRED);
        }
        a.setSubmittedAt(a.getEndsAt());
        a.setDraftAnswers(null);
        a.setDraftEssay(null);
        attempts.save(a);
        events.emit("assessment.attempt.expired", "attempt", a.getId(),
                Map.of("family", a.getFamily().name(), "status", a.getStatus().name()));
    }

    private Attempt owned(UUID attemptId) {
        Attempt a = attempts.findById(attemptId)
                .orElseThrow(() -> ApplicationException.notFound("Attempt not found"));
        if (!a.getStudentId().equals(SecurityUtils.currentAccountId()) && !SecurityUtils.isAdmin()) {
            throw ApplicationException.notFound("Attempt not found");
        }
        return a;
    }

    private StartSpec resolve(UUID unitId) {
        IeltsUnit ielts = ieltsUnits.findById(unitId).orElse(null);
        if (ielts != null) {
            return new StartSpec(Assembler.familyOf(ielts.getSkill()), ielts.getTitle(),
                    ielts.getDurationSeconds(), ielts.getSectionData(), ielts.getPassage(), ielts.getPrompt(),
                    ielts.getAudioId(), ielts.getImageId(), ielts.getProblems(), ielts.isActive());
        }
        ObjectiveUnit obj = objectiveUnits.findById(unitId)
                .orElseThrow(() -> ApplicationException.notFound("Unit not found"));
        AttemptFamily family = obj.getKind() == UnitKind.SAT ? AttemptFamily.SAT : AttemptFamily.QUIZ;
        return new StartSpec(family, obj.getTitle(), obj.getDurationSeconds(),
                null, null, null, null, null, obj.getQuestions(), obj.isActive());
    }

    private Map<UUID, List<String>> toUuidMap(List<AnswerInput> answers) {
        Map<UUID, List<String>> map = new HashMap<>();
        if (answers != null) {
            for (AnswerInput a : answers) {
                if (a != null && a.problemId() != null) {
                    map.put(a.problemId(), a.values() == null ? List.of() : a.values());
                }
            }
        }
        return map;
    }

    private Map<String, List<String>> toStringMap(List<AnswerInput> answers) {
        Map<String, List<String>> map = new HashMap<>();
        if (answers != null) {
            for (AnswerInput a : answers) {
                if (a != null && a.problemId() != null) {
                    map.put(a.problemId().toString(), a.values() == null ? List.of() : a.values());
                }
            }
        }
        return map;
    }

    private Map<UUID, List<String>> fromStringMap(Map<String, List<String>> draft) {
        Map<UUID, List<String>> map = new HashMap<>();
        if (draft != null) {
            draft.forEach((k, v) -> map.put(UUID.fromString(k), v == null ? List.of() : v));
        }
        return map;
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private record StartSpec(AttemptFamily family, String title, int duration, String sectionData,
                             String passage, String prompt, UUID audioId, UUID imageId,
                             List<StoredProblem> problems, boolean active) {
    }
}
