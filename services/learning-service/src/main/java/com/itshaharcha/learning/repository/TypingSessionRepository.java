package com.itshaharcha.learning.repository;

import com.itshaharcha.learning.entity.TypingSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface TypingSessionRepository extends JpaRepository<TypingSession, UUID> {

    // Native query with explicit timestamptz casts: a bare ":from IS NULL" leaves
    // Postgres unable to infer the bind-parameter type ("could not determine data
    // type of parameter"). Casting the nullable bounds fixes that.
    @Query(value = """
            SELECT * FROM typing_sessions s
            WHERE s.account_id = :accountId
              AND s.deleted = false
              AND (CAST(:from AS timestamptz) IS NULL OR s.created_at >= :from)
              AND (CAST(:to AS timestamptz) IS NULL OR s.created_at <= :to)
            """,
            countQuery = """
            SELECT count(*) FROM typing_sessions s
            WHERE s.account_id = :accountId
              AND s.deleted = false
              AND (CAST(:from AS timestamptz) IS NULL OR s.created_at >= :from)
              AND (CAST(:to AS timestamptz) IS NULL OR s.created_at <= :to)
            """,
            nativeQuery = true)
    Page<TypingSession> search(@Param("accountId") UUID accountId,
                               @Param("from") Instant from,
                               @Param("to") Instant to,
                               Pageable pageable);

    long countByAccountId(UUID accountId);
}
