package com.itshaharcha.portfolio.repository;

import com.itshaharcha.portfolio.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EducationRepository extends JpaRepository<Education, UUID> {

    List<Education> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    Optional<Education> findByIdAndAccountId(UUID id, UUID accountId);

    long countByAccountId(UUID accountId);
}
