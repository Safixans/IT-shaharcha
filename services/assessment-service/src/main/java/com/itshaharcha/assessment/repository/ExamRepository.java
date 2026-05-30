package com.itshaharcha.assessment.repository;

import com.itshaharcha.assessment.entity.Exam;
import com.itshaharcha.assessment.entity.ExamType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ExamRepository extends JpaRepository<Exam, UUID> {

    @Query("""
            SELECT e FROM Exam e
            WHERE (:examType IS NULL OR e.examType = :examType)
            """)
    Page<Exam> search(@Param("examType") ExamType examType, Pageable pageable);
}
