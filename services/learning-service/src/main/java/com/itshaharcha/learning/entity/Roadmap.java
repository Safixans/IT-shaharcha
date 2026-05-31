package com.itshaharcha.learning.entity;

import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "roadmaps")
public class Roadmap extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    private String title;

    private String tagline;

    @Column(columnDefinition = "text")
    private String description;

    private String icon;

    @Column(nullable = false)
    private String kind = "role";

    @Column(nullable = false)
    private String status = "draft";

    @Column(name = "layout_mode", nullable = false)
    private String layoutMode = "auto";

    private String difficulty;

    @Column(name = "est_hours")
    private Integer estHours;

    @Column(name = "published_at")
    private Instant publishedAt;
}
