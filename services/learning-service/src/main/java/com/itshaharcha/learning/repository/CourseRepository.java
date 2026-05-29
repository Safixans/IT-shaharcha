package com.itshaharcha.learning.repository;

import com.itshaharcha.learning.entity.Course;
import com.itshaharcha.learning.entity.Level;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {

    @Query("""
            SELECT c FROM Course c
            WHERE (:trackId IS NULL OR c.trackId = :trackId)
              AND (:level IS NULL OR c.level = :level)
            """)
    Page<Course> search(@Param("trackId") UUID trackId,
                         @Param("level") Level level,
                         Pageable pageable);

    long countByTrackId(UUID trackId);
}
