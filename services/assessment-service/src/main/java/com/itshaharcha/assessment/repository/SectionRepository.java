package com.itshaharcha.assessment.repository;

import com.itshaharcha.assessment.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SectionRepository extends JpaRepository<Section, UUID> {

    List<Section> findByExamIdOrderByOrderIndexAsc(UUID examId);

    long countByExamId(UUID examId);
}
