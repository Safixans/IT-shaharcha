package com.itshaharcha.learning.entity;

import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tracks")
public class Track extends BaseEntity {

    @Column(nullable = false)
    private String title;

    private String slug;

    @Column(columnDefinition = "text")
    private String description;
}
