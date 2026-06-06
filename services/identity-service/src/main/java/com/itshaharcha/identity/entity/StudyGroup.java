package com.itshaharcha.identity.entity;

import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * A teacher's group. Kept independent of the account domain — references the teacher
 * by id (UUID) rather than a JPA relation, so the group and account domains stay decoupled.
 * Named {@code StudyGroup} (not {@code Group}) because {@code GROUP} is reserved in HQL/JPQL.
 */
@Getter
@Setter
@Entity
@Table(name = "study_groups")
public class StudyGroup extends BaseEntity {

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "teacher_id", nullable = false)
    private UUID teacherId;
}
