package com.itshaharcha.identity.repository;

import com.itshaharcha.identity.entity.GroupMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, UUID> {

    Optional<GroupMembership> findByStudentId(UUID studentId);

    boolean existsByStudentId(UUID studentId);

    List<GroupMembership> findByGroupId(UUID groupId);

    Optional<GroupMembership> findByGroupIdAndStudentId(UUID groupId, UUID studentId);

    long countByGroupId(UUID groupId);

    /** Memberships across every group owned by a teacher (theta-join — domains stay decoupled). */
    @Query("""
            select m from GroupMembership m, StudyGroup g
            where g.id = m.groupId and g.teacherId = :teacherId
            """)
    List<GroupMembership> findByTeacher(@Param("teacherId") UUID teacherId);

    /** True if the student is in one of the teacher's groups (grading authorization). */
    @Query("""
            select count(m) > 0 from GroupMembership m, StudyGroup g
            where g.id = m.groupId and g.teacherId = :teacherId and m.studentId = :studentId
            """)
    boolean isStudentOf(@Param("teacherId") UUID teacherId, @Param("studentId") UUID studentId);
}
