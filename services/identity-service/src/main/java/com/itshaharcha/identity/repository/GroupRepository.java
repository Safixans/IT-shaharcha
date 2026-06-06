package com.itshaharcha.identity.repository;

import com.itshaharcha.identity.entity.StudyGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GroupRepository extends JpaRepository<StudyGroup, UUID> {

    Page<StudyGroup> findByTeacherId(UUID teacherId, Pageable pageable);
}
