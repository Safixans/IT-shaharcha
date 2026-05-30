package com.itshaharcha.assessment.repository;

import com.itshaharcha.assessment.entity.ExamResult;
import com.itshaharcha.assessment.entity.ExamType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ExamResultRepository extends JpaRepository<ExamResult, UUID> {

    Optional<ExamResult> findBySessionId(UUID sessionId);

    long countByAccountId(UUID accountId);

    @Query("""
            SELECT r FROM ExamResult r
            WHERE r.accountId = :accountId
              AND (:examType IS NULL OR r.examType = :examType)
              AND (:from IS NULL OR r.scoredAt >= :from)
              AND (:to IS NULL OR r.scoredAt < :to)
            """)
    Page<ExamResult> search(@Param("accountId") UUID accountId,
                            @Param("examType") ExamType examType,
                            @Param("from") Instant from,
                            @Param("to") Instant to,
                            Pageable pageable);
}
