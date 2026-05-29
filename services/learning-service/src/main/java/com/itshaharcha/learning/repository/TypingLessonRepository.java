package com.itshaharcha.learning.repository;

import com.itshaharcha.learning.entity.Level;
import com.itshaharcha.learning.entity.TypingLesson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TypingLessonRepository extends JpaRepository<TypingLesson, UUID> {

    @Query("SELECT t FROM TypingLesson t WHERE (:difficulty IS NULL OR t.difficulty = :difficulty)")
    Page<TypingLesson> search(@Param("difficulty") Level difficulty, Pageable pageable);
}
