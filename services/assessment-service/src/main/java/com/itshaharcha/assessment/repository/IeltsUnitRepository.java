package com.itshaharcha.assessment.repository;

import com.itshaharcha.assessment.entity.IeltsUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface IeltsUnitRepository extends JpaRepository<IeltsUnit, UUID> {

    /**
     * Browse by skill, optionally active-only and tag-filtered. {@code tag} is a JSON array
     * literal like {@code ["academic"]} (or null) matched with the jsonb containment operator.
     */
    @Query(value = """
            SELECT * FROM ielts_units u
            WHERE u.skill = :skill
              AND (:activeOnly = FALSE OR u.active = TRUE)
              AND (CAST(:tag AS text) IS NULL OR u.tags @> CAST(:tag AS jsonb))
            ORDER BY u.created_at DESC
            """,
            countQuery = """
            SELECT count(*) FROM ielts_units u
            WHERE u.skill = :skill
              AND (:activeOnly = FALSE OR u.active = TRUE)
              AND (CAST(:tag AS text) IS NULL OR u.tags @> CAST(:tag AS jsonb))
            """,
            nativeQuery = true)
    Page<IeltsUnit> search(@Param("skill") String skill,
                           @Param("activeOnly") boolean activeOnly,
                           @Param("tag") String tag,
                           Pageable pageable);
}
