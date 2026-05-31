package com.itshaharcha.learning.repository;

import com.itshaharcha.learning.entity.RoadmapNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoadmapNodeRepository extends JpaRepository<RoadmapNode, UUID> {

    List<RoadmapNode> findByRoadmapIdOrderByOrderIndexAsc(UUID roadmapId);

    long countByRoadmapId(UUID roadmapId);
}
