package com.itshaharcha.learning.repository;

import com.itshaharcha.learning.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ModuleRepository extends JpaRepository<Module, UUID> {

    List<Module> findByCourseIdOrderByOrderIndexAsc(UUID courseId);
}
