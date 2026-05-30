package com.itshaharcha.assessment.service.impl;

import com.itshaharcha.assessment.dto.request.Answer;
import com.itshaharcha.assessment.dto.request.SaveAnswersInput;
import com.itshaharcha.assessment.dto.request.SubmitInput;
import com.itshaharcha.assessment.dto.response.ExamResultResponse;
import com.itshaharcha.assessment.dto.response.ExamSessionResponse;
import com.itshaharcha.assessment.dto.response.SessionQuestionResponse;
import com.itshaharcha.assessment.entity.Exam;
import com.itshaharcha.assessment.entity.ExamResult;
import com.itshaharcha.assessment.entity.ExamSession;
import com.itshaharcha.assessment.entity.ExamType;
import com.itshaharcha.assessment.entity.Question;
import com.itshaharcha.assessment.entity.Section;
import com.itshaharcha.assessment.entity.SessionAnswer;
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
import com.itshaharcha.assessment.security.SecurityUtils;
import com.itshaharcha.assessment.service.SessionService;
import com.itshaharcha.common.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final ExamRepository examRepository;
    private final SectionRepository sectionRepository;
    private final QuestionRepository questionRepository;
    private final ExamSessionRepository sessionRepository;
    private final SessionAnswerRepository answerRepository;
    private final ExamResultRepository resultRepository;
    private final AssessmentMapper mapper;
    private final Scorer scorer;
    private final AssessmentEventPublisher events;

    @Override
    @Transactional
    public ExamSessionResponse start(UUID examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> ApplicationException.notFound("Exam not found"));
        UUID accountId = SecurityUtils.currentAccountId();

        ExamSession resumed = sessionRepository
                .findByExamIdAndAccountIdAndStatus(examId, accountId, SessionStatus.in_progress)
                .orElse(null);
        if (resumed != null) {
            return mapper.toSessionResponse(resumed);
        }

        Instant now = Instant.now();
        ExamSession session = new ExamSession();
        session.setExamId(examId);
        session.setAccountId(accountId);
        session.setStatus(SessionStatus.in_progress);
        session.setStartedAt(now);
        if (exam.getDurationMinutes() != null) {
            session.setExpiresAt(now.plus(exam.getDurationMinutes(), ChronoUnit.MINUTES));
        }
        ExamSession saved = sessionRepository.save(session);
        events.emit("assessment.exam.started", "session", saved.getId(),
                Map.of("examId", examId.toString(), "examType", exam.getExamType().name()));
        return mapper.toSessionResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ExamSessionResponse getSession(UUID sessionId) {
        return mapper.toSessionResponse(requireOwnedSession(sessionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionQuestionResponse> getSessionQuestions(UUID sessionId) {
        ExamSession session = requireOwnedSession(sessionId);
        return questionRepository.findByExamIdOrderByOrderIndexAsc(session.getExamId()).stream()
                .map(mapper::toSessionQuestion)
                .toList();
    }

    @Override
    @Transactional
    public void saveAnswers(UUID sessionId, SaveAnswersInput input) {
        ExamSession session = requireOwnedSession(sessionId);
        requireInProgress(session);
        input.answers().forEach(answer -> upsertAnswer(sessionId, answer));
    }

    @Override
    @Transactional
    public ExamResultResponse submit(UUID sessionId, SubmitInput input) {
        ExamSession session = requireOwnedSession(sessionId);

        if (session.getStatus() == SessionStatus.scored) {
            ExamResult existing = resultRepository.findBySessionId(sessionId)
                    .orElseThrow(() -> ApplicationException.notFound("Result not found"));
            return mapper.toResultResponse(existing);
        }
        if (session.getStatus() == SessionStatus.expired) {
            throw ApplicationException.conflict("Session has expired");
        }

        if (input != null && input.answers() != null) {
            input.answers().forEach(answer -> upsertAnswer(sessionId, answer));
        }

        Exam exam = examRepository.findById(session.getExamId())
                .orElseThrow(() -> ApplicationException.notFound("Exam not found"));
        List<Section> sections = sectionRepository.findByExamIdOrderByOrderIndexAsc(exam.getId());
        List<Question> questions = questionRepository.findByExamIdOrderByOrderIndexAsc(exam.getId());
        Map<UUID, Object> answers = answerRepository.findBySessionId(sessionId).stream()
                .filter(a -> a.getValue() != null)
                .collect(Collectors.toMap(SessionAnswer::getQuestionId, SessionAnswer::getValue, (a, b) -> b));

        Scorer.ScoreResult scored = scorer.score(questions, sections, answers);

        Instant now = Instant.now();
        ExamResult result = new ExamResult();
        result.setSessionId(sessionId);
        result.setExamId(exam.getId());
        result.setAccountId(session.getAccountId());
        result.setExamType(exam.getExamType());
        result.setScaledScore(scored.scaledScore());
        result.setMaxScore(scored.maxScore());
        result.setSectionScores(scored.sectionScores());
        result.setScoredAt(now);
        ExamResult savedResult = resultRepository.save(result);

        session.setStatus(SessionStatus.scored);
        session.setSubmittedAt(now);
        sessionRepository.save(session);

        Map<String, Object> eventData = Map.of(
                "examId", exam.getId().toString(),
                "examType", exam.getExamType().name(),
                "scaledScore", scored.scaledScore(),
                "maxScore", scored.maxScore());
        events.emit("assessment.exam.submitted", "session", sessionId, eventData);
        events.emit("assessment.exam.scored", "session", sessionId, eventData);
        if (exam.getExamType() == ExamType.MOCK) {
            events.emit("assessment.mock.completed", "session", sessionId, eventData);
        }
        return mapper.toResultResponse(savedResult);
    }

    private void upsertAnswer(UUID sessionId, Answer answer) {
        SessionAnswer entity = answerRepository
                .findBySessionIdAndQuestionId(sessionId, answer.questionId())
                .orElseGet(() -> {
                    SessionAnswer created = new SessionAnswer();
                    created.setSessionId(sessionId);
                    created.setQuestionId(answer.questionId());
                    return created;
                });
        entity.setSectionId(answer.sectionId());
        entity.setValue(answer.value());
        answerRepository.save(entity);
    }

    private ExamSession requireOwnedSession(UUID sessionId) {
        ExamSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> ApplicationException.notFound("Session not found"));
        if (!session.getAccountId().equals(SecurityUtils.currentAccountId()) && !SecurityUtils.isAdmin()) {
            throw ApplicationException.forbidden("Cannot access another candidate's session");
        }
        return session;
    }

    private void requireInProgress(ExamSession session) {
        if (session.getStatus() != SessionStatus.in_progress) {
            throw ApplicationException.conflict("Session is no longer open for answers");
        }
    }
}
