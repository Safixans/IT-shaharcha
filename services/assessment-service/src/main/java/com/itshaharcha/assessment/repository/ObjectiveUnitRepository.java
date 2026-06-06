package com.itshaharcha.assessment.repository;

import com.itshaharcha.assessment.entity.ObjectiveUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ObjectiveUnitRepository extends JpaRepository<ObjectiveUnit, UUID> {

    @Query(value = """
            SELECT * FROM objective_units u
            WHERE u.kind = :kind
              AND (:activeOnly = FALSE OR u.active = TRUE)
              AND (CAST(:section AS text) IS NULL OR u.sat_section = :section)
              AND (CAST(:tag AS text) IS NULL OR u.tags @> CAST(:tag AS jsonb))
            ORDER BY u.created_at DESC
            """,
            countQuery = """
            SELECT count(*) FROM objective_units u
            WHERE u.kind = :kind
              AND (:activeOnly = FALSE OR u.active = TRUE)
              AND (CAST(:section AS text) IS NULL OR u.sat_section = :section)
              AND (CAST(:tag AS text) IS NULL OR u.tags @> CAST(:tag AS jsonb))
            """,
            nativeQuery = true)
    Page<ObjectiveUnit> search(@Param("kind") String kind,
                               @Param("activeOnly") boolean activeOnly,
                               @Param("section") String section,
                               @Param("tag") String tag,
                               Pageable pageable);
}
