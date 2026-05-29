package com.itshaharcha.learning.repository;

import com.itshaharcha.learning.entity.SourceSyncRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SourceSyncRunRepository extends JpaRepository<SourceSyncRun, UUID> {
}
