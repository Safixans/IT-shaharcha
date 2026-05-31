package com.itshaharcha.learning.repository;

import com.itshaharcha.learning.entity.RoadmapEdge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoadmapEdgeRepository extends JpaRepository<RoadmapEdge, UUID> {

    List<RoadmapEdge> findByRoadmapId(UUID roadmapId);
}
