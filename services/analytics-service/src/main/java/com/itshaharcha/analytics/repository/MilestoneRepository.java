package com.itshaharcha.analytics.repository;

import com.itshaharcha.analytics.entity.MilestoneRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MilestoneRepository extends JpaRepository<MilestoneRecord, UUID> {

    boolean existsByAccountIdAndMilestone(UUID accountId, String milestone);

    Page<MilestoneRecord> findByAccountIdOrderByReachedAtDesc(UUID accountId, Pageable pageable);
}
