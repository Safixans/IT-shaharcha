package com.itshaharcha.assessment.service;

import com.itshaharcha.assessment.domain.StoredOption;
import com.itshaharcha.assessment.domain.StoredProblem;
import com.itshaharcha.assessment.dto.request.ObjectiveQuestionInput;
import com.itshaharcha.assessment.dto.request.ObjectiveUnitCreate;
import com.itshaharcha.assessment.dto.response.PageResponse;
import com.itshaharcha.assessment.dto.response.UnitDetail;
import com.itshaharcha.assessment.dto.response.UnitMeta;
import com.itshaharcha.assessment.entity.ObjectiveUnit;
import com.itshaharcha.assessment.entity.ProblemType;
import com.itshaharcha.assessment.entity.SatSection;
import com.itshaharcha.assessment.entity.UnitKind;
import com.itshaharcha.assessment.event.AssessmentEventPublisher;
import com.itshaharcha.assessment.mapper.Assembler;
import com.itshaharcha.assessment.repository.ObjectiveUnitRepository;
import com.itshaharcha.common.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Authoring + browsing of objective units (SAT modules and generic quizzes). SAT and QUIZ share
 * this engine; the only difference is the activation gate and the optional {@code satSection}.
 */
@Service
@RequiredArgsConstructor
public class ObjectiveUnitService {

    private static final int SAT_RW_QUESTIONS = 27;
    private static final int SAT_MATH_QUESTIONS = 22;

    private final ObjectiveUnitRepository repository;
    private final AssessmentEventPublisher events;

    @Transactional(readOnly = true)
    public PageResponse<UnitMeta> browse(UnitKind kind, SatSection section, boolean activeOnly,
                                         List<String> tags, Pageable pageable) {
        String tag = TagSupport.firstTagLiteral(tags);
        return PageResponse.from(
                repository.search(kind.name(), activeOnly, section == null ? null : section.name(), tag, pageable),
                Assembler::meta);
    }

    @Transactional(readOnly = true)
    public UnitDetail get(UUID id) {
        return Assembler.detail(load(id));
    }

    @Transactional
    public UnitDetail create(UnitKind kind, ObjectiveUnitCreate req) {
        if (kind == UnitKind.SAT && req.satSection() == null) {
            throw ApplicationException.badRequest("SAT modules require a satSection");
        }
        ObjectiveUnit u = new ObjectiveUnit();
        u.setKind(kind);
        u.setSatSection(kind == UnitKind.SAT ? req.satSection() : null);
        u.setTitle(req.title());
        u.setTags(req.tags() == null ? new ArrayList<>() : new ArrayList<>(req.tags()));

        List<StoredProblem> questions = req.questions().stream().map(this::toProblem).toList();
        u.setQuestions(questions);
        u.setProblemCount(questions.stream()
                .mapToInt(p -> p.type() == ProblemType.MULTI_SELECT ? p.correctCount() : 1).sum());
        u.setDurationSeconds(req.durationSeconds() != null ? req.durationSeconds() : 1800);
        u.setActive(false);

        ObjectiveUnit saved = repository.save(u);
        events.emit("assessment." + kind.name().toLowerCase() + ".created", "objectiveUnit", saved.getId(),
                Map.of("kind", kind.name(), "problemCount", saved.getProblemCount()));
        return Assembler.detail(saved);
    }

    @Transactional
    public void delete(UUID id) {
        ObjectiveUnit u = load(id);
        repository.delete(u);
        events.emit("assessment.objective.unit.deleted", "objectiveUnit", u.getId(),
                Map.of("kind", u.getKind().name()));
    }

    @Transactional
    public UnitDetail setActive(UUID id, boolean active) {
        ObjectiveUnit u = load(id);
        if (active) {
            guardActivation(u);
        }
        u.setActive(active);
        repository.save(u);
        events.emit("assessment.objective.unit." + (active ? "activated" : "deactivated"),
                "objectiveUnit", u.getId(), Map.of("kind", u.getKind().name()));
        return Assembler.detail(u);
    }

    // ---- helpers ----

    private StoredProblem toProblem(ObjectiveQuestionInput q) {
        UUID id = UUID.randomUUID();
        if (q.type() == ProblemType.INPUT) {
            List<String> accepted = q.correctAnswers() == null ? List.of() : q.correctAnswers().stream()
                    .map(String::trim).filter(s -> !s.isEmpty()).toList();
            if (accepted.isEmpty()) {
                throw ApplicationException.badRequest("INPUT question requires at least one correct answer");
            }
            List<StoredOption> options = accepted.stream()
                    .map(a -> new StoredOption(null, a, true)).toList();
            return new StoredProblem(id, ProblemType.INPUT, q.prompt(), options, 1);
        }

        if (q.options() == null || q.options().isEmpty()) {
            throw ApplicationException.badRequest("Choice question requires options");
        }
        List<StoredOption> options = new ArrayList<>();
        int correct = 0;
        for (int i = 0; i < q.options().size(); i++) {
            var in = q.options().get(i);
            if (in.text() == null || in.text().isBlank()) {
                throw ApplicationException.badRequest("Option text is required");
            }
            options.add(new StoredOption(String.valueOf(i + 1), in.text().trim(), in.correct()));
            if (in.correct()) {
                correct++;
            }
        }
        if (correct == 0) {
            throw ApplicationException.badRequest("Question has no correct option");
        }
        int correctCount = q.type() == ProblemType.MULTI_SELECT ? correct : 1;
        return new StoredProblem(id, q.type(), q.prompt(), options, correctCount);
    }

    private void guardActivation(ObjectiveUnit u) {
        if (u.getKind() == UnitKind.SAT) {
            int required = u.getSatSection() == SatSection.MATH ? SAT_MATH_QUESTIONS : SAT_RW_QUESTIONS;
            if (u.getProblemCount() != required) {
                throw ApplicationException.conflict(
                        "SAT %s modules need exactly %d questions to activate".formatted(u.getSatSection(), required));
            }
        } else if (u.getProblemCount() < 1) {
            throw ApplicationException.conflict("A quiz needs at least one question to activate");
        }
    }

    private ObjectiveUnit load(UUID id) {
        return repository.findById(id).orElseThrow(() -> ApplicationException.notFound("Unit not found"));
    }
}
