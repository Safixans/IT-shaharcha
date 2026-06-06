package com.itshaharcha.assessment.service;

import com.itshaharcha.assessment.client.IdentityClient;
import com.itshaharcha.assessment.dto.request.WritingGrade;
import com.itshaharcha.assessment.dto.response.AttemptReport;
import com.itshaharcha.assessment.dto.response.PageResponse;
import com.itshaharcha.assessment.entity.Attempt;
import com.itshaharcha.assessment.entity.AttemptFamily;
import com.itshaharcha.assessment.entity.AttemptStatus;
import com.itshaharcha.assessment.event.AssessmentEventPublisher;
import com.itshaharcha.assessment.mapper.Assembler;
import com.itshaharcha.assessment.repository.AttemptRepository;
import com.itshaharcha.assessment.security.SecurityUtils;
import com.itshaharcha.common.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Teacher-facing Writing grading. The grading queue and the grade action are both scoped to the
 * teacher's own students — resolved by forwarding the caller's token to identity-service.
 */
@Service
@RequiredArgsConstructor
public class GradingService {

    private final AttemptRepository attempts;
    private final IdentityClient identity;
    private final AssessmentEventPublisher events;

    @Transactional(readOnly = true)
    public PageResponse<AttemptReport> queue(Pageable pageable) {
        List<UUID> studentIds = identity.myStudentIds();
        if (studentIds.isEmpty()) {
            return PageResponse.from(Page.empty(pageable), Assembler::report);
        }
        Page<Attempt> page = attempts.findByFamilyAndStatusAndStudentIdInOrderBySubmittedAtAsc(
                AttemptFamily.IELTS_WRITING, AttemptStatus.PENDING_GRADING, studentIds, pageable);
        return PageResponse.from(page, Assembler::report);
    }

    @Transactional
    public AttemptReport gradeWriting(UUID attemptId, WritingGrade grade) {
        Attempt a = attempts.findById(attemptId)
                .orElseThrow(() -> ApplicationException.notFound("Attempt not found"));
        if (a.getFamily() != AttemptFamily.IELTS_WRITING) {
            throw ApplicationException.badRequest("Only Writing attempts are teacher-graded");
        }
        if (a.getStatus() != AttemptStatus.PENDING_GRADING && a.getStatus() != AttemptStatus.GRADED) {
            throw ApplicationException.conflict("Attempt is not awaiting grading");
        }
        if (!SecurityUtils.isAdmin() && !identity.isMyStudent(a.getStudentId())) {
            throw ApplicationException.forbidden("Not your student");
        }

        a.setBand(grade.band());
        a.setCriteria(grade.criteria());
        a.setFeedback(grade.feedback());
        a.setGradedBy(SecurityUtils.currentAccountId());
        a.setGradedAt(Instant.now());
        a.setStatus(AttemptStatus.GRADED);
        Attempt saved = attempts.save(a);
        events.emit("assessment.writing.graded", "attempt", saved.getId(),
                Map.of("studentId", saved.getStudentId().toString(), "band", grade.band()));
        return Assembler.report(saved);
    }
}
