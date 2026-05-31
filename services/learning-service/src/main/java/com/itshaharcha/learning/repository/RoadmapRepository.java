package com.itshaharcha.learning.repository;

import com.itshaharcha.learning.entity.Roadmap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RoadmapRepository extends JpaRepository<Roadmap, UUID> {

    Optional<Roadmap> findBySlug(String slug);

    @Query("""
            SELECT r FROM Roadmap r
            WHERE r.status = 'published'
              AND (CAST(:kind AS string) IS NULL OR r.kind = :kind)
              AND (CAST(:q AS string) IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))
            ORDER BY r.title ASC
            """)
    Page<Roadmap> searchPublished(@Param("q") String q,
                                  @Param("kind") String kind,
                                  Pageable pageable);
}
