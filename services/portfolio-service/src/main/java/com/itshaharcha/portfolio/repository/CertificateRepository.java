package com.itshaharcha.portfolio.repository;

import com.itshaharcha.portfolio.entity.Certificate;
import com.itshaharcha.portfolio.entity.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {

    Page<Certificate> findByAccountId(UUID accountId, Pageable pageable);

    List<Certificate> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    Optional<Certificate> findByIdAndAccountId(UUID id, UUID accountId);

    long countByAccountId(UUID accountId);

    long countByAccountIdAndStatus(UUID accountId, VerificationStatus status);
}
