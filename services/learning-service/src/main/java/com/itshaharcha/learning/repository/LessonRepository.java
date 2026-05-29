package com.itshaharcha.learning.repository;

import com.itshaharcha.learning.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findByModuleIdOrderByOrderIndexAsc(UUID moduleId);

    @Query("""
            SELECT l FROM Lesson l
            WHERE l.moduleId IN (SELECT m.id FROM Module m WHERE m.courseId = :courseId)
            """)
    List<Lesson> findByCourseId(@Param("courseId") UUID courseId);

    @Query("""
            SELECT COUNT(l) FROM Lesson l
            WHERE l.moduleId IN (SELECT m.id FROM Module m WHERE m.courseId = :courseId)
            """)
    long countByCourseId(@Param("courseId") UUID courseId);
}
