package com.itshaharcha.assessment.repository;

import com.itshaharcha.assessment.entity.Attempt;
import com.itshaharcha.assessment.entity.AttemptFamily;
import com.itshaharcha.assessment.entity.AttemptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttemptRepository extends JpaRepository<Attempt, UUID> {

    Optional<Attempt> findFirstByStudentIdAndFamilyAndStatusOrderByStartedAtDesc(
            UUID studentId, AttemptFamily family, AttemptStatus status);

    Page<Attempt> findByStudentIdOrderByStartedAtDesc(UUID studentId, Pageable pageable);

    Page<Attempt> findByStudentIdAndFamilyOrderByStartedAtDesc(
            UUID studentId, AttemptFamily family, Pageable pageable);

    /** Pending-grading writing attempts for a set of students (the teacher's roster). */
    Page<Attempt> findByFamilyAndStatusAndStudentIdInOrderBySubmittedAtAsc(
            AttemptFamily family, AttemptStatus status, List<UUID> studentIds, Pageable pageable);
}
