package com.itshaharcha.assessment.service;

import com.itshaharcha.assessment.dto.request.SubmitInput;
import com.itshaharcha.assessment.entity.Exam;
import com.itshaharcha.assessment.entity.ExamResult;
import com.itshaharcha.assessment.entity.ExamSession;
import com.itshaharcha.assessment.entity.ExamType;
import com.itshaharcha.assessment.entity.SessionStatus;
import com.itshaharcha.assessment.event.AssessmentEventPublisher;
import com.itshaharcha.assessment.mapper.AssessmentMapper;
import com.itshaharcha.assessment.repository.ExamRepository;
import com.itshaharcha.assessment.repository.ExamResultRepository;
import com.itshaharcha.assessment.repository.ExamSessionRepository;
import com.itshaharcha.assessment.repository.QuestionRepository;
import com.itshaharcha.assessment.repository.SectionRepository;
import com.itshaharcha.assessment.repository.SessionAnswerRepository;
import com.itshaharcha.assessment.scoring.Scorer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    @Mock private ExamRepository examRepository;
    @Mock private SectionRepository sectionRepository;
    @Mock private QuestionRepository questionRepository;
    @Mock private ExamSessionRepository sessionRepository;
    @Mock private SessionAnswerRepository answerRepository;
    @Mock private ExamResultRepository resultRepository;
    @Mock private AssessmentMapper mapper;
    @Mock private Scorer scorer;
    @Mock private AssessmentEventPublisher events;
    @InjectMocks private com.itshaharcha.assessment.service.impl.SessionServiceImpl service;

    private final UUID accountId = UUID.randomUUID();

    @BeforeEach
    void setAuth() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(accountId, null,
                        List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))));
    }

    @AfterEach
    void clearAuth() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void start_newSession_savesAndEmits() {
        UUID examId = UUID.randomUUID();
        Exam exam = new Exam();
        exam.setExamType(ExamType.PRACTICE);
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(sessionRepository.findByExamIdAndAccountIdAndStatus(examId, accountId, SessionStatus.in_progress))
                .thenReturn(Optional.empty());
        when(sessionRepository.save(any(ExamSession.class))).thenAnswer(i -> {
            ExamSession s = i.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        service.start(examId);

        verify(sessionRepository).save(any(ExamSession.class));
        verify(events).emit(eq("assessment.exam.started"), eq("session"), any(UUID.class), any());
    }

    @Test
    void start_resumesInProgress_noNewSaveOrEmit() {
        UUID examId = UUID.randomUUID();
        Exam exam = new Exam();
        exam.setExamType(ExamType.PRACTICE);
        ExamSession existing = new ExamSession();
        existing.setAccountId(accountId);
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(sessionRepository.findByExamIdAndAccountIdAndStatus(examId, accountId, SessionStatus.in_progress))
                .thenReturn(Optional.of(existing));

        service.start(examId);

        verify(sessionRepository, never()).save(any());
        verify(events, never()).emit(any(), any(), any(), any());
    }

    @Test
    void submit_scoresPersistsResultAndEmitsScoredEvents() {
        UUID sessionId = UUID.randomUUID();
        UUID examId = UUID.randomUUID();
        ExamSession session = new ExamSession();
        session.setAccountId(accountId);
        session.setExamId(examId);
        session.setStatus(SessionStatus.in_progress);
        Exam exam = new Exam();
        exam.setId(examId);
        exam.setExamType(ExamType.MOCK);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(sectionRepository.findByExamIdOrderByOrderIndexAsc(examId)).thenReturn(List.of());
        when(questionRepository.findByExamIdOrderByOrderIndexAsc(examId)).thenReturn(List.of());
        when(answerRepository.findBySessionId(sessionId)).thenReturn(List.of());
        when(scorer.score(any(), any(), any()))
                .thenReturn(new Scorer.ScoreResult(7.0, 10.0, List.of()));
        when(resultRepository.save(any(ExamResult.class))).thenAnswer(i -> i.getArgument(0));

        service.submit(sessionId, new SubmitInput(null));

        verify(resultRepository).save(any(ExamResult.class));
        verify(events).emit(eq("assessment.exam.submitted"), eq("session"), eq(sessionId), any());
        verify(events).emit(eq("assessment.exam.scored"), eq("session"), eq(sessionId), any());
        verify(events).emit(eq("assessment.mock.completed"), eq("session"), eq(sessionId), any());
        org.assertj.core.api.Assertions.assertThat(session.getStatus()).isEqualTo(SessionStatus.scored);
        org.assertj.core.api.Assertions.assertThat(session.getSubmittedAt()).isNotNull();
    }

    @Test
    void submit_alreadyScored_returnsExistingResultWithoutRescoring() {
        UUID sessionId = UUID.randomUUID();
        ExamSession session = new ExamSession();
        session.setAccountId(accountId);
        session.setStatus(SessionStatus.scored);
        ExamResult existing = new ExamResult();
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(resultRepository.findBySessionId(sessionId)).thenReturn(Optional.of(existing));

        service.submit(sessionId, new SubmitInput(null));

        verify(scorer, never()).score(any(), any(), any());
        verify(resultRepository, never()).save(any());
        verify(mapper).toResultResponse(existing);
    }
}
