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
@Table(name = "roadmap_nodes")
public class RoadmapNode extends BaseEntity {

    @Column(name = "roadmap_id", nullable = false)
    private UUID roadmapId;

    @Column(name = "node_key", nullable = false)
    private String nodeKey;

    @Column(nullable = false)
    private String type = "topic";

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String summary;

    @Column(columnDefinition = "text")
    private String detail;

    @Column(nullable = false)
    private boolean optional = false;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "pos_x")
    private Double posX;

    @Column(name = "pos_y")
    private Double posY;

    @Column(name = "order_index", nullable = false)
    private int orderIndex = 0;
}
