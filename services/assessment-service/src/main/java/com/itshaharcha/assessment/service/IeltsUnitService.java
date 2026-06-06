package com.itshaharcha.assessment.service;

import com.itshaharcha.assessment.content.HtmlBlotParser;
import com.itshaharcha.assessment.content.ParsedContent;
import com.itshaharcha.assessment.dto.request.ListeningCreate;
import com.itshaharcha.assessment.dto.request.ReadingCreate;
import com.itshaharcha.assessment.dto.request.WritingCreate;
import com.itshaharcha.assessment.dto.response.PageResponse;
import com.itshaharcha.assessment.dto.response.UnitDetail;
import com.itshaharcha.assessment.dto.response.UnitMeta;
import com.itshaharcha.assessment.entity.IeltsSkill;
import com.itshaharcha.assessment.entity.IeltsUnit;
import com.itshaharcha.assessment.entity.WritingTask;
import com.itshaharcha.assessment.event.AssessmentEventPublisher;
import com.itshaharcha.assessment.mapper.Assembler;
import com.itshaharcha.assessment.repository.IeltsUnitRepository;
import com.itshaharcha.assessment.security.SecurityUtils;
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
 * Authoring + browsing of single-skill IELTS units. Content is parsed once (delete+recreate
 * semantics): any edit re-parses the HTML, overwrites the immutable answer key, and forces
 * re-activation. Listening default 30 min, reading 20 min, writing 20/40 min per task.
 */
@Service
@RequiredArgsConstructor
public class IeltsUnitService {

    private final IeltsUnitRepository repository;
    private final HtmlBlotParser parser;
    private final AssessmentEventPublisher events;

    @Transactional(readOnly = true)
    public PageResponse<UnitMeta> browse(IeltsSkill skill, boolean activeOnly, List<String> tags, Pageable pageable) {
        String tag = TagSupport.firstTagLiteral(tags);
        return PageResponse.from(repository.search(skill.name(), activeOnly, tag, pageable), Assembler::meta);
    }

    @Transactional(readOnly = true)
    public UnitDetail get(UUID id) {
        // Authors get the answer-bearing originalSectionData (to edit); students get the stripped view.
        return Assembler.detail(load(id), SecurityUtils.isAuthor());
    }

    @Transactional
    public UnitDetail createListening(ListeningCreate req) {
        IeltsUnit u = new IeltsUnit();
        u.setSkill(IeltsSkill.LISTENING);
        applyListening(u, req);
        return persist(u, "assessment.ielts.listening.created");
    }

    @Transactional
    public UnitDetail updateListening(UUID id, ListeningCreate req) {
        IeltsUnit u = loadSkill(id, IeltsSkill.LISTENING);
        applyListening(u, req);
        u.setActive(false);
        return persist(u, "assessment.ielts.listening.updated");
    }

    @Transactional
    public UnitDetail createReading(ReadingCreate req) {
        IeltsUnit u = new IeltsUnit();
        u.setSkill(IeltsSkill.READING);
        applyReading(u, req);
        return persist(u, "assessment.ielts.reading.created");
    }

    @Transactional
    public UnitDetail updateReading(UUID id, ReadingCreate req) {
        IeltsUnit u = loadSkill(id, IeltsSkill.READING);
        applyReading(u, req);
        u.setActive(false);
        return persist(u, "assessment.ielts.reading.updated");
    }

    @Transactional
    public UnitDetail createWriting(WritingCreate req) {
        IeltsUnit u = new IeltsUnit();
        u.setSkill(IeltsSkill.WRITING);
        u.setTitle(req.title());
        u.setTags(req.tags() == null ? new ArrayList<>() : new ArrayList<>(req.tags()));
        u.setWritingTask(req.task());
        u.setPrompt(req.prompt());
        u.setImageId(req.imageId());
        u.setProblemCount(0);
        u.setDurationSeconds(req.durationSeconds() != null ? req.durationSeconds()
                : (req.task() == WritingTask.TASK_1 ? 1200 : 2400));
        u.setActive(false);
        return persist(u, "assessment.ielts.writing.created");
    }

    @Transactional
    public void delete(UUID id) {
        IeltsUnit u = load(id);
        repository.delete(u);
        events.emit("assessment.ielts.unit.deleted", "ieltsUnit", u.getId(), Map.of("skill", u.getSkill().name()));
    }

    @Transactional
    public UnitDetail setActive(UUID id, boolean active) {
        IeltsUnit u = load(id);
        if (active) {
            guardActivation(u);
        }
        u.setActive(active);
        repository.save(u);
        events.emit("assessment.ielts.unit." + (active ? "activated" : "deactivated"),
                "ieltsUnit", u.getId(), Map.of("skill", u.getSkill().name()));
        return Assembler.detail(u, true);
    }

    // ---- helpers ----

    private void applyListening(IeltsUnit u, ListeningCreate req) {
        u.setTitle(req.title());
        u.setTags(req.tags() == null ? new ArrayList<>() : new ArrayList<>(req.tags()));
        u.setAudioId(req.audioId());
        ParsedContent pc = parser.parse(req.questions());
        u.setOriginalSectionData(req.questions());
        u.setSectionData(pc.sectionData());
        u.setProblems(pc.problems());
        u.setProblemCount(pc.problemCount());
        u.setDurationSeconds(req.durationSeconds() != null ? req.durationSeconds() : 1800);
    }

    private void applyReading(IeltsUnit u, ReadingCreate req) {
        u.setTitle(req.title());
        u.setTags(req.tags() == null ? new ArrayList<>() : new ArrayList<>(req.tags()));
        u.setPassage(req.passage());
        ParsedContent pc = parser.parse(req.questions());
        u.setOriginalSectionData(req.questions());
        u.setSectionData(pc.sectionData());
        u.setProblems(pc.problems());
        u.setProblemCount(pc.problemCount());
        u.setDurationSeconds(req.durationSeconds() != null ? req.durationSeconds() : 1200);
    }

    private void guardActivation(IeltsUnit u) {
        switch (u.getSkill()) {
            case LISTENING -> {
                if (u.getProblemCount() != 40) {
                    throw ApplicationException.conflict("Listening units must have exactly 40 questions to activate");
                }
            }
            case READING -> {
                if (u.getProblemCount() != 13 && u.getProblemCount() != 14) {
                    throw ApplicationException.conflict("Reading units must have 13–14 questions to activate");
                }
            }
            case WRITING -> {
                if (u.getWritingTask() == WritingTask.TASK_1 && u.getImageId() == null) {
                    throw ApplicationException.conflict("Writing Task 1 requires an image to activate");
                }
            }
        }
    }

    private UnitDetail persist(IeltsUnit u, String eventType) {
        IeltsUnit saved = repository.save(u);
        events.emit(eventType, "ieltsUnit", saved.getId(),
                Map.of("skill", saved.getSkill().name(), "problemCount", saved.getProblemCount()));
        return Assembler.detail(saved, true);
    }

    private IeltsUnit load(UUID id) {
        return repository.findById(id).orElseThrow(() -> ApplicationException.notFound("Unit not found"));
    }

    private IeltsUnit loadSkill(UUID id, IeltsSkill skill) {
        IeltsUnit u = load(id);
        if (u.getSkill() != skill) {
            throw ApplicationException.notFound("Unit not found");
        }
        return u;
    }
}
