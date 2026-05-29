package com.itshaharcha.learning.repository;

import com.itshaharcha.learning.entity.ContentSource;
import com.itshaharcha.learning.entity.SourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ContentSourceRepository extends JpaRepository<ContentSource, UUID> {

    @Query("SELECT s FROM ContentSource s WHERE (:type IS NULL OR s.type = :type)")
    Page<ContentSource> search(@Param("type") SourceType type, Pageable pageable);
}
