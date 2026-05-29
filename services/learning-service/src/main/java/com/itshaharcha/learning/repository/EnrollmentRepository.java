package com.itshaharcha.learning.repository;

import com.itshaharcha.learning.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    Page<Enrollment> findByAccountId(UUID accountId, Pageable pageable);

    Optional<Enrollment> findByCourseIdAndAccountId(UUID courseId, UUID accountId);
}
