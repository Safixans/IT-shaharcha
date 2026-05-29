package com.itshaharcha.learning.service.impl;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.learning.dto.request.LessonCompletedInput;
import com.itshaharcha.learning.dto.response.EnrollmentResponse;
import com.itshaharcha.learning.dto.response.LessonProgressResponse;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.entity.Enrollment;
import com.itshaharcha.learning.entity.EnrollmentStatus;
import com.itshaharcha.learning.entity.Lesson;
import com.itshaharcha.learning.entity.LessonProgress;
import com.itshaharcha.learning.event.LearningEventPublisher;
import com.itshaharcha.learning.mapper.LearningMapper;
import com.itshaharcha.learning.repository.CourseRepository;
import com.itshaharcha.learning.repository.EnrollmentRepository;
import com.itshaharcha.learning.repository.LessonProgressRepository;
import com.itshaharcha.learning.repository.LessonRepository;
import com.itshaharcha.learning.repository.ModuleRepository;
import com.itshaharcha.learning.security.SecurityUtils;
import com.itshaharcha.learning.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final LearningMapper mapper;
    private final LearningEventPublisher events;

    @Override
    @Transactional
    public EnrollmentResponse enroll(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw ApplicationException.notFound("Course not found");
        }
        UUID accountId = SecurityUtils.currentAccountId();
        Enrollment existing = enrollmentRepository.findByCourseIdAndAccountId(courseId, accountId)
                .orElse(null);
        if (existing != null) {
            return mapper.toEnrollmentResponse(existing);
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(courseId);
        enrollment.setAccountId(accountId);
        enrollment.setStatus(EnrollmentStatus.active);
        enrollment.setProgressPercent(0);
        enrollment.setEnrolledAt(Instant.now());
        Enrollment saved = enrollmentRepository.save(enrollment);
        events.emit("learning.course.enrolled", "course", courseId,
                Map.of("enrollmentId", saved.getId().toString()));
        return mapper.toEnrollmentResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EnrollmentResponse> listMyEnrollments(Pageable pageable) {
        UUID accountId = SecurityUtils.currentAccountId();
        return PageResponse.from(
                enrollmentRepository.findByAccountId(accountId, pageable),
                mapper::toEnrollmentResponse);
    }

    @Override
    @Transactional
    public void startLesson(UUID lessonId) {
        Lesson lesson = requireLesson(lessonId);
        UUID accountId = SecurityUtils.currentAccountId();
        UUID courseId = courseIdOf(lesson);
        LessonProgress progress = lessonProgressRepository.findByLessonIdAndAccountId(lessonId, accountId)
                .orElseGet(() -> newProgress(lessonId, courseId, accountId));
        if (progress.getStartedAt() == null) {
            progress.setStartedAt(Instant.now());
            lessonProgressRepository.save(progress);
        }
        events.emit("learning.lesson.started", "lesson", lessonId,
                Map.of("courseId", courseId.toString()));
    }

    @Override
    @Transactional
    public LessonProgressResponse completeLesson(UUID lessonId, LessonCompletedInput input) {
        Lesson lesson = requireLesson(lessonId);
        UUID accountId = SecurityUtils.currentAccountId();
        UUID courseId = input.courseId() != null ? input.courseId() : courseIdOf(lesson);
        LessonProgress progress = lessonProgressRepository.findByLessonIdAndAccountId(lessonId, accountId)
                .orElseGet(() -> newProgress(lessonId, courseId, accountId));
        progress.setCourseId(courseId);
        if (progress.getStartedAt() == null) {
            progress.setStartedAt(Instant.now());
        }
        progress.setCompleted(true);
        progress.setScorePercent(input.scorePercent());
        progress.setCompletedAt(Instant.now());
        LessonProgress saved = lessonProgressRepository.save(progress);

        double coursePercent = recomputeEnrollmentProgress(courseId, accountId);
        events.emit("learning.lesson.completed", "lesson", lessonId, Map.of(
                "courseId", courseId.toString(),
                "durationSeconds", input.durationSeconds(),
                "courseProgressPercent", coursePercent));
        return mapper.toLessonProgressResponse(saved, coursePercent);
    }

    private double recomputeEnrollmentProgress(UUID courseId, UUID accountId) {
        long total = lessonRepository.countByCourseId(courseId);
        long completed = lessonProgressRepository
                .countByCourseIdAndAccountIdAndCompletedTrue(courseId, accountId);
        double percent = total == 0 ? 0 : Math.round(completed * 10000.0 / total) / 100.0;
        enrollmentRepository.findByCourseIdAndAccountId(courseId, accountId).ifPresent(enrollment -> {
            enrollment.setProgressPercent(percent);
            if (total > 0 && completed >= total) {
                enrollment.setStatus(EnrollmentStatus.completed);
                if (enrollment.getCompletedAt() == null) {
                    enrollment.setCompletedAt(Instant.now());
                }
            }
            enrollmentRepository.save(enrollment);
        });
        return percent;
    }

    private LessonProgress newProgress(UUID lessonId, UUID courseId, UUID accountId) {
        LessonProgress progress = new LessonProgress();
        progress.setLessonId(lessonId);
        progress.setCourseId(courseId);
        progress.setAccountId(accountId);
        return progress;
    }

    private UUID courseIdOf(Lesson lesson) {
        return moduleRepository.findById(lesson.getModuleId())
                .map(com.itshaharcha.learning.entity.Module::getCourseId)
                .orElseThrow(() -> ApplicationException.notFound("Course not found for lesson"));
    }

    private Lesson requireLesson(UUID id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("Lesson not found"));
    }
}
