package com.itshaharcha.learning.service;

import com.itshaharcha.learning.dto.request.TypingLessonInput;
import com.itshaharcha.learning.dto.request.TypingSessionInput;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.dto.response.TypingLessonResponse;
import com.itshaharcha.learning.dto.response.TypingSessionResponse;
import com.itshaharcha.learning.entity.Level;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface TypingService {

    PageResponse<TypingLessonResponse> listTypingLessons(Level difficulty, Pageable pageable);

    TypingSessionResponse submitSession(TypingSessionInput input);

    PageResponse<TypingSessionResponse> listMySessions(Instant from, Instant to, Pageable pageable);

    TypingLessonResponse createTypingLesson(TypingLessonInput input);

    TypingLessonResponse updateTypingLesson(UUID lessonId, TypingLessonInput input);

    void deleteTypingLesson(UUID lessonId);
}
