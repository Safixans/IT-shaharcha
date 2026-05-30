package com.itshaharcha.assessment.repository;

import com.itshaharcha.assessment.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findByExamIdOrderByOrderIndexAsc(UUID examId);

    List<Question> findBySectionIdOrderByOrderIndexAsc(UUID sectionId);

    long countBySectionId(UUID sectionId);
}
