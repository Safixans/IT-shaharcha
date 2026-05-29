package com.itshaharcha.learning.repository;

import com.itshaharcha.learning.entity.Tutorial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TutorialRepository extends JpaRepository<Tutorial, UUID> {

    @Query("SELECT t FROM Tutorial t WHERE (:topic IS NULL OR t.topic = :topic)")
    Page<Tutorial> search(@Param("topic") String topic, Pageable pageable);

    long countBySourceId(UUID sourceId);
}
