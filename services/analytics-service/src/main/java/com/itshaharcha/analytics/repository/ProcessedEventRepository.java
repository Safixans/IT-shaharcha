package com.itshaharcha.analytics.repository;

import com.itshaharcha.analytics.entity.ProcessedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

    boolean existsByEventId(UUID eventId);

    Page<ProcessedEvent> findByAccountIdOrderByOccurredAtDesc(UUID accountId, Pageable pageable);
}
