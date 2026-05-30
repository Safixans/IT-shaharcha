package com.itshaharcha.assessment.repository;

import com.itshaharcha.assessment.entity.SessionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionAnswerRepository extends JpaRepository<SessionAnswer, UUID> {

    List<SessionAnswer> findBySessionId(UUID sessionId);

    Optional<SessionAnswer> findBySessionIdAndQuestionId(UUID sessionId, UUID questionId);
}
