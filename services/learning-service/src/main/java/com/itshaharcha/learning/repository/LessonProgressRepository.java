package com.itshaharcha.learning.repository;

import com.itshaharcha.learning.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID> {

    Optional<LessonProgress> findByLessonIdAndAccountId(UUID lessonId, UUID accountId);

    long countByCourseIdAndAccountIdAndCompletedTrue(UUID courseId, UUID accountId);

    long countByAccountIdAndCompletedTrue(UUID accountId);
}
