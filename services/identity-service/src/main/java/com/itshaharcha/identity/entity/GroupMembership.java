package com.itshaharcha.identity.entity;

import com.itshaharcha.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** A student's membership in a group. {@code studentId} is unique → one group per student. */
@Getter
@Setter
@Entity
@Table(name = "group_memberships")
public class GroupMembership extends BaseEntity {

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "student_id", nullable = false, unique = true)
    private UUID studentId;
}
