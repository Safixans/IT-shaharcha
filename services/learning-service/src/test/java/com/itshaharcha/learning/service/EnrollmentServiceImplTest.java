package com.itshaharcha.learning.service;

import com.itshaharcha.learning.dto.request.LessonCompletedInput;
import com.itshaharcha.learning.entity.Enrollment;
import com.itshaharcha.learning.entity.EnrollmentStatus;
import com.itshaharcha.learning.entity.Lesson;
import com.itshaharcha.learning.entity.LessonProgress;
import com.itshaharcha.learning.entity.Module;
import com.itshaharcha.learning.event.LearningEventPublisher;
import com.itshaharcha.learning.mapper.LearningMapper;
import com.itshaharcha.learning.repository.CourseRepository;
import com.itshaharcha.learning.repository.EnrollmentRepository;
import com.itshaharcha.learning.repository.LessonProgressRepository;
import com.itshaharcha.learning.repository.LessonRepository;
import com.itshaharcha.learning.repository.ModuleRepository;
import com.itshaharcha.learning.service.impl.EnrollmentServiceImpl;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private ModuleRepository moduleRepository;
    @Mock private LessonRepository lessonRepository;
    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private LearningMapper mapper;
    @Mock private LearningEventPublisher events;
    @InjectMocks private EnrollmentServiceImpl service;

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
    void enroll_newEnrollment_savesAndEmits() {
        UUID courseId = UUID.randomUUID();
        when(courseRepository.existsById(courseId)).thenReturn(true);
        when(enrollmentRepository.findByCourseIdAndAccountId(courseId, accountId))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(i -> {
            Enrollment e = i.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        service.enroll(courseId);

        verify(enrollmentRepository).save(any(Enrollment.class));
        verify(events).emit(eq("learning.course.enrolled"), eq("course"), eq(courseId), any());
    }

    @Test
    void enroll_existing_isIdempotent_noEmit() {
        UUID courseId = UUID.randomUUID();
        Enrollment existing = new Enrollment();
        when(courseRepository.existsById(courseId)).thenReturn(true);
        when(enrollmentRepository.findByCourseIdAndAccountId(courseId, accountId))
                .thenReturn(Optional.of(existing));

        service.enroll(courseId);

        verify(enrollmentRepository, never()).save(any());
        verify(events, never()).emit(any(), any(), any(), any());
    }

    @Test
    void completeLesson_marksProgressAndRecomputesEnrollmentToCompleted() {
        UUID lessonId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        Lesson lesson = new Lesson();
        lesson.setModuleId(UUID.randomUUID());
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(lessonProgressRepository.findByLessonIdAndAccountId(lessonId, accountId))
                .thenReturn(Optional.empty());
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(i -> i.getArgument(0));
        when(lessonRepository.countByCourseId(courseId)).thenReturn(2L);
        when(lessonProgressRepository.countByCourseIdAndAccountIdAndCompletedTrue(courseId, accountId))
                .thenReturn(2L);
        Enrollment enrollment = new Enrollment();
        enrollment.setStatus(EnrollmentStatus.active);
        when(enrollmentRepository.findByCourseIdAndAccountId(courseId, accountId))
                .thenReturn(Optional.of(enrollment));

        LessonCompletedInput input = new LessonCompletedInput(courseId, null, 120, 90.0);
        service.completeLesson(lessonId, input);

        verify(mapper).toLessonProgressResponse(any(LessonProgress.class), eq(100.0));
        verify(events).emit(eq("learning.lesson.completed"), eq("lesson"), eq(lessonId), any());
        org.assertj.core.api.Assertions.assertThat(enrollment.getStatus())
                .isEqualTo(EnrollmentStatus.completed);
        org.assertj.core.api.Assertions.assertThat(enrollment.getProgressPercent()).isEqualTo(100.0);
    }

    @Test
    void startLesson_resolvesCourseViaModule_andEmits() {
        UUID lessonId = UUID.randomUUID();
        UUID moduleId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        Lesson lesson = new Lesson();
        lesson.setModuleId(moduleId);
        Module module = new Module();
        module.setCourseId(courseId);
        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(module));
        when(lessonProgressRepository.findByLessonIdAndAccountId(lessonId, accountId))
                .thenReturn(Optional.empty());

        service.startLesson(lessonId);

        verify(lessonProgressRepository).save(any(LessonProgress.class));
        verify(events).emit(eq("learning.lesson.started"), eq("lesson"), eq(lessonId), any());
    }
}
