package com.itshaharcha.identity.service;

import com.itshaharcha.identity.dto.request.GroupInput;
import com.itshaharcha.identity.dto.request.GroupUpdate;
import com.itshaharcha.identity.dto.response.GroupResponse;
import com.itshaharcha.identity.dto.response.MemberResponse;
import com.itshaharcha.identity.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface GroupService {

    GroupResponse create(GroupInput input);

    PageResponse<GroupResponse> list(UUID teacherId, Pageable pageable);

    GroupResponse get(UUID groupId, UUID callerId, boolean privileged);

    GroupResponse update(UUID groupId, GroupUpdate input);

    void delete(UUID groupId);

    MemberResponse addStudent(UUID groupId, UUID studentId, UUID callerId, boolean privileged);

    void removeStudent(UUID groupId, UUID studentId, UUID callerId, boolean privileged);

    List<MemberResponse> listMembers(UUID groupId, UUID callerId, boolean privileged);

    /** Students across all of a teacher's groups. */
    List<MemberResponse> studentsOf(UUID teacherId);

    /** Grading authorization: is this student in one of the teacher's groups? */
    boolean isStudentOf(UUID teacherId, UUID studentId);
}
