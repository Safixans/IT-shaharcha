package com.itshaharcha.assessment.service;

import com.itshaharcha.assessment.domain.StoredOption;
import com.itshaharcha.assessment.domain.StoredProblem;
import com.itshaharcha.assessment.dto.request.AnswerInput;
import com.itshaharcha.assessment.dto.request.AttemptSubmit;
import com.itshaharcha.assessment.dto.response.AttemptReport;
import com.itshaharcha.assessment.dto.response.AttemptSession;
import com.itshaharcha.assessment.entity.Attempt;
import com.itshaharcha.assessment.entity.AttemptFamily;
import com.itshaharcha.assessment.entity.AttemptStatus;
import com.itshaharcha.assessment.entity.IeltsSkill;
import com.itshaharcha.assessment.entity.IeltsUnit;
import com.itshaharcha.assessment.entity.ProblemType;
import com.itshaharcha.assessment.event.AssessmentEventPublisher;
import com.itshaharcha.assessment.repository.AttemptRepository;
import com.itshaharcha.assessment.repository.IeltsUnitRepository;
import com.itshaharcha.assessment.repository.ObjectiveUnitRepository;
import com.itshaharcha.assessment.scoring.Grader;
import com.itshaharcha.common.exception.ApplicationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttemptServiceTest {

    @Mock private AttemptRepository attempts;
    @Mock private IeltsUnitRepository ieltsUnits;
    @Mock private ObjectiveUnitRepository objectiveUnits;
    @Mock private AssessmentEventPublisher events;

    private AttemptService service;

    private final UUID student = UUID.randomUUID();
    private final UUID problemId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new AttemptService(attempts, ieltsUnits, objectiveUnits, new Grader(), events);
        authenticate(student);
        lenient().when(attempts.save(any(Attempt.class))).thenAnswer(i -> {
            Attempt a = i.getArgument(0);
            if (a.getId() == null) {
                a.setId(UUID.randomUUID());
            }
            return a;
        });
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void start_createsAttemptWithSnapshotAndServerComputedTiming() {
        IeltsUnit unit = listeningUnit();
        when(ieltsUnits.findById(unit.getId())).thenReturn(Optional.of(unit));
        when(attempts.findFirstByStudentIdAndFamilyAndStatusOrderByStartedAtDesc(
                student, AttemptFamily.IELTS_LISTENING, AttemptStatus.IN_PROGRESS)).thenReturn(Optional.empty());

        AttemptSession s = service.start(unit.getId());

        assertThat(s.status()).isEqualTo(AttemptStatus.IN_PROGRESS);
        assertThat(s.family()).isEqualTo(AttemptFamily.IELTS_LISTENING);
        assertThat(s.timing().endsAt()).isAfter(s.timing().startedAt());
        assertThat(s.timing().remainingSeconds()).isBetween(1700L, 1800L);
        // Listening problems are served without options (the widgets live in the HTML).
        assertThat(s.problems()).hasSize(1);
        assertThat(s.problems().get(0).options()).isNull();
        verify(attempts).save(any(Attempt.class));
    }

    @Test
    void start_resumesSameRunningUnitWithoutCreatingNew() {
        IeltsUnit unit = listeningUnit();
        Attempt running = inProgressAttempt(unit.getId(), Instant.now().plusSeconds(900));
        when(ieltsUnits.findById(unit.getId())).thenReturn(Optional.of(unit));
        when(attempts.findFirstByStudentIdAndFamilyAndStatusOrderByStartedAtDesc(
                student, AttemptFamily.IELTS_LISTENING, AttemptStatus.IN_PROGRESS)).thenReturn(Optional.of(running));

        AttemptSession s = service.start(unit.getId());

        assertThat(s.attemptId()).isEqualTo(running.getId());
        verify(attempts, never()).save(any(Attempt.class));
    }

    @Test
    void start_finalizesExpiredAttemptThenCreatesNew() {
        IeltsUnit unit = listeningUnit();
        Attempt expired = inProgressAttempt(unit.getId(), Instant.now().minusSeconds(10));
        expired.setDraftAnswers(new HashMap<>(Map.of(problemId.toString(), List.of("snails"))));
        when(ieltsUnits.findById(unit.getId())).thenReturn(Optional.of(unit));
        when(attempts.findFirstByStudentIdAndFamilyAndStatusOrderByStartedAtDesc(
                student, AttemptFamily.IELTS_LISTENING, AttemptStatus.IN_PROGRESS)).thenReturn(Optional.of(expired));

        AttemptSession s = service.start(unit.getId());

        assertThat(expired.getStatus()).isEqualTo(AttemptStatus.EXPIRED);
        assertThat(expired.getCorrectCount()).isEqualTo(1); // graded from the autosaved draft
        assertThat(s.attemptId()).isNotEqualTo(expired.getId());
        assertThat(s.status()).isEqualTo(AttemptStatus.IN_PROGRESS);
        verify(attempts, times(2)).save(any(Attempt.class)); // finalize + create
    }

    @Test
    void submit_gradesObjectiveAndComputesListeningBand() {
        Attempt a = inProgressAttempt(UUID.randomUUID(), Instant.now().plusSeconds(600));
        when(attempts.findById(a.getId())).thenReturn(Optional.of(a));

        AttemptReport r = service.submit(a.getId(),
                new AttemptSubmit(List.of(new AnswerInput(problemId, List.of("snails"))), null));

        assertThat(r.status()).isEqualTo(AttemptStatus.COMPLETED);
        assertThat(r.correct()).isEqualTo(1);
        assertThat(r.scorePercent()).isEqualTo(100.0);
        assertThat(r.band()).isEqualTo(2.0); // BandTable.listening(1)
    }

    @Test
    void submit_afterDeadlineScoresAutosavedDraftNotLatePayload() {
        Attempt a = inProgressAttempt(UUID.randomUUID(), Instant.now().minusSeconds(5));
        a.setDraftAnswers(new HashMap<>(Map.of(problemId.toString(), List.of("snails"))));
        when(attempts.findById(a.getId())).thenReturn(Optional.of(a));

        AttemptReport r = service.submit(a.getId(),
                new AttemptSubmit(List.of(new AnswerInput(problemId, List.of("WRONG"))), null));

        assertThat(r.correct()).isEqualTo(1); // used the saved draft, not the late "WRONG"
    }

    @Test
    void submit_isIdempotentOnceTerminal() {
        Attempt a = inProgressAttempt(UUID.randomUUID(), Instant.now().plusSeconds(600));
        a.setStatus(AttemptStatus.COMPLETED);
        when(attempts.findById(a.getId())).thenReturn(Optional.of(a));

        AttemptReport r = service.submit(a.getId(), new AttemptSubmit(List.of(), null));

        assertThat(r.status()).isEqualTo(AttemptStatus.COMPLETED);
        verify(attempts, never()).save(any(Attempt.class));
    }

    @Test
    void autosave_persistsDraftAnswers() {
        Attempt a = inProgressAttempt(UUID.randomUUID(), Instant.now().plusSeconds(600));
        when(attempts.findById(a.getId())).thenReturn(Optional.of(a));

        service.autosave(a.getId(), new AttemptSubmit(List.of(new AnswerInput(problemId, List.of("snails"))), null));

        assertThat(a.getDraftAnswers()).containsEntry(problemId.toString(), List.of("snails"));
        verify(attempts).save(a);
    }

    @Test
    void accessingAnotherStudentsAttemptIsNotFound() {
        Attempt a = inProgressAttempt(UUID.randomUUID(), Instant.now().plusSeconds(600));
        when(attempts.findById(a.getId())).thenReturn(Optional.of(a));
        authenticate(UUID.randomUUID()); // a different (non-admin) student

        assertThatThrownBy(() -> service.get(a.getId())).isInstanceOf(ApplicationException.class);
    }

    // ---- fixtures ----

    private IeltsUnit listeningUnit() {
        IeltsUnit u = new IeltsUnit();
        u.setId(UUID.randomUUID());
        u.setSkill(IeltsSkill.LISTENING);
        u.setTitle("Listening 1");
        u.setActive(true);
        u.setDurationSeconds(1800);
        u.setSectionData("<p>The animal is <span data-problem-id=\"x\"></span></p>");
        u.setProblems(List.of(answerKey()));
        u.setProblemCount(1);
        return u;
    }

    private Attempt inProgressAttempt(UUID unitId, Instant endsAt) {
        Attempt a = new Attempt();
        a.setId(UUID.randomUUID());
        a.setStudentId(student);
        a.setUnitId(unitId);
        a.setFamily(AttemptFamily.IELTS_LISTENING);
        a.setStatus(AttemptStatus.IN_PROGRESS);
        a.setTitle("Listening 1");
        a.setStartedAt(endsAt.minusSeconds(1800));
        a.setEndsAt(endsAt);
        a.setSnapshotProblems(List.of(answerKey()));
        return a;
    }

    private StoredProblem answerKey() {
        return new StoredProblem(problemId, ProblemType.INPUT, null,
                List.of(new StoredOption(null, "snails", true)), 1);
    }

    private void authenticate(UUID accountId) {
        var auth = new UsernamePasswordAuthenticationToken(accountId, null,
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
