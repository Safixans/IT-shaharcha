package com.itshaharcha.learning.repository;

import com.itshaharcha.learning.entity.Doc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DocRepository extends JpaRepository<Doc, UUID> {

    @Query("SELECT d FROM Doc d WHERE (:topic IS NULL OR d.topic = :topic)")
    Page<Doc> search(@Param("topic") String topic, Pageable pageable);

    long countBySourceId(UUID sourceId);
}
