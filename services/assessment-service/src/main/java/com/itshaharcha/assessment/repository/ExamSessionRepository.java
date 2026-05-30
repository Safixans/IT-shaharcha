package com.itshaharcha.assessment.repository;

import com.itshaharcha.assessment.entity.ExamSession;
import com.itshaharcha.assessment.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ExamSessionRepository extends JpaRepository<ExamSession, UUID> {

    Optional<ExamSession> findByExamIdAndAccountIdAndStatus(UUID examId, UUID accountId, SessionStatus status);

    long countByAccountId(UUID accountId);
}
