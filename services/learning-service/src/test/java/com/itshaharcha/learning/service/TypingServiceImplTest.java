package com.itshaharcha.learning.service;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.exception.ErrorCode;
import com.itshaharcha.learning.dto.request.TypingSessionInput;
import com.itshaharcha.learning.entity.TypingSession;
import com.itshaharcha.learning.event.LearningEventPublisher;
import com.itshaharcha.learning.mapper.LearningMapper;
import com.itshaharcha.learning.repository.TypingLessonRepository;
import com.itshaharcha.learning.repository.TypingSessionRepository;
import com.itshaharcha.learning.service.impl.TypingServiceImpl;
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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TypingServiceImplTest {

    @Mock private TypingLessonRepository typingLessonRepository;
    @Mock private TypingSessionRepository typingSessionRepository;
    @Mock private LearningMapper mapper;
    @Mock private LearningEventPublisher events;
    @InjectMocks private TypingServiceImpl service;

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
    void submitSession_persistsForCallerAndEmits() {
        when(typingSessionRepository.save(any(TypingSession.class))).thenAnswer(i -> i.getArgument(0));
        TypingSessionInput input = new TypingSessionInput(75.0, 95.0, 60, 300, null);

        service.submitSession(input);

        verify(typingSessionRepository).save(any(TypingSession.class));
        verify(events).emit(eq("learning.typing.session.completed"), eq("typingSession"), any(), any());
    }

    @Test
    void submitSession_unknownLesson_throwsNotFound() {
        UUID lessonId = UUID.randomUUID();
        when(typingLessonRepository.existsById(lessonId)).thenReturn(false);
        TypingSessionInput input = new TypingSessionInput(75.0, 95.0, 60, 300, lessonId);

        assertThatThrownBy(() -> service.submitSession(input))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);

        verify(typingSessionRepository, never()).save(any());
    }
}
