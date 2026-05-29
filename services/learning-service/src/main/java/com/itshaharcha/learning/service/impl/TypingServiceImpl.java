package com.itshaharcha.learning.service.impl;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.learning.dto.request.TypingLessonInput;
import com.itshaharcha.learning.dto.request.TypingSessionInput;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.dto.response.TypingLessonResponse;
import com.itshaharcha.learning.dto.response.TypingSessionResponse;
import com.itshaharcha.learning.entity.Level;
import com.itshaharcha.learning.entity.TypingLesson;
import com.itshaharcha.learning.entity.TypingSession;
import com.itshaharcha.learning.event.LearningEventPublisher;
import com.itshaharcha.learning.mapper.LearningMapper;
import com.itshaharcha.learning.repository.TypingLessonRepository;
import com.itshaharcha.learning.repository.TypingSessionRepository;
import com.itshaharcha.learning.security.SecurityUtils;
import com.itshaharcha.learning.service.TypingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TypingServiceImpl implements TypingService {

    private final TypingLessonRepository typingLessonRepository;
    private final TypingSessionRepository typingSessionRepository;
    private final LearningMapper mapper;
    private final LearningEventPublisher events;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TypingLessonResponse> listTypingLessons(Level difficulty, Pageable pageable) {
        return PageResponse.from(typingLessonRepository.search(difficulty, pageable),
                mapper::toTypingLessonResponse);
    }

    @Override
    @Transactional
    public TypingSessionResponse submitSession(TypingSessionInput input) {
        UUID accountId = SecurityUtils.currentAccountId();
        if (input.lessonId() != null && !typingLessonRepository.existsById(input.lessonId())) {
            throw ApplicationException.notFound("Typing lesson not found");
        }
        TypingSession session = new TypingSession();
        session.setAccountId(accountId);
        session.setLessonId(input.lessonId());
        session.setWpm(input.wpm());
        session.setAccuracyPercent(input.accuracyPercent());
        session.setDurationSeconds(input.durationSeconds());
        session.setKeystrokes(input.keystrokes());
        TypingSession saved = typingSessionRepository.save(session);
        events.emit("learning.typing.session.completed", "typingSession", saved.getId(), Map.of(
                "wpm", input.wpm(),
                "accuracyPercent", input.accuracyPercent(),
                "durationSeconds", input.durationSeconds()));
        return mapper.toTypingSessionResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TypingSessionResponse> listMySessions(Instant from, Instant to, Pageable pageable) {
        UUID accountId = SecurityUtils.currentAccountId();
        return PageResponse.from(typingSessionRepository.search(accountId, from, to, pageable),
                mapper::toTypingSessionResponse);
    }

    @Override
    @Transactional
    public TypingLessonResponse createTypingLesson(TypingLessonInput input) {
        TypingLesson lesson = new TypingLesson();
        apply(lesson, input);
        return mapper.toTypingLessonResponse(typingLessonRepository.save(lesson));
    }

    @Override
    @Transactional
    public TypingLessonResponse updateTypingLesson(UUID lessonId, TypingLessonInput input) {
        TypingLesson lesson = require(lessonId);
        apply(lesson, input);
        return mapper.toTypingLessonResponse(typingLessonRepository.save(lesson));
    }

    @Override
    @Transactional
    public void deleteTypingLesson(UUID lessonId) {
        typingLessonRepository.delete(require(lessonId));
    }

    private void apply(TypingLesson lesson, TypingLessonInput input) {
        lesson.setTitle(input.title());
        lesson.setDifficulty(input.difficulty());
        lesson.setText(input.text());
    }

    private TypingLesson require(UUID id) {
        return typingLessonRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("Typing lesson not found"));
    }
}
