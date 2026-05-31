package com.itshaharcha.learning.entity;

import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "roadmap_edges")
public class RoadmapEdge extends BaseEntity {

    @Column(name = "roadmap_id", nullable = false)
    private UUID roadmapId;

    @Column(name = "from_node_id", nullable = false)
    private UUID fromNodeId;

    @Column(name = "to_node_id", nullable = false)
    private UUID toNodeId;

    @Column(nullable = false)
    private String kind = "sequence";

    @Column(nullable = false)
    private String style = "solid";
}
