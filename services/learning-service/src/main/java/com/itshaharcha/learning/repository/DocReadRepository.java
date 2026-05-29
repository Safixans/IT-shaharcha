package com.itshaharcha.learning.repository;

import com.itshaharcha.learning.entity.DocRead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocReadRepository extends JpaRepository<DocRead, UUID> {

    Optional<DocRead> findByDocIdAndAccountId(UUID docId, UUID accountId);

    long countByAccountId(UUID accountId);
}
