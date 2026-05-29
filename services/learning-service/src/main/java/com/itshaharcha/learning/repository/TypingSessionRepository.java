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

    @Query("""
            SELECT s FROM TypingSession s
            WHERE s.accountId = :accountId
              AND (:from IS NULL OR s.createdAt >= :from)
              AND (:to IS NULL OR s.createdAt <= :to)
            """)
    Page<TypingSession> search(@Param("accountId") UUID accountId,
                               @Param("from") Instant from,
                               @Param("to") Instant to,
                               Pageable pageable);

    long countByAccountId(UUID accountId);
}
