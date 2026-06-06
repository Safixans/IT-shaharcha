package com.itshaharcha.identity.service.impl;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.identity.dto.request.GroupInput;
import com.itshaharcha.identity.dto.request.GroupUpdate;
import com.itshaharcha.identity.dto.response.GroupResponse;
import com.itshaharcha.identity.dto.response.MemberResponse;
import com.itshaharcha.identity.dto.response.PageResponse;
import com.itshaharcha.identity.entity.Account;
import com.itshaharcha.identity.entity.GroupMembership;
import com.itshaharcha.identity.entity.StudyGroup;
import com.itshaharcha.identity.entity.Role;
import com.itshaharcha.identity.repository.AccountRepository;
import com.itshaharcha.identity.repository.GroupMembershipRepository;
import com.itshaharcha.identity.repository.GroupRepository;
import com.itshaharcha.identity.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private static final String ROLE_TEACHER = "ROLE_TEACHER";
    private static final String ROLE_STUDENT = "ROLE_STUDENT";

    private final GroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public GroupResponse create(GroupInput input) {
        Account teacher = requireAccount(input.teacherId());
        requireRole(teacher, ROLE_TEACHER, "Group owner must be a teacher");
        StudyGroup group = new StudyGroup();
        group.setName(input.name().trim());
        group.setTeacherId(teacher.getId());
        StudyGroup saved = groupRepository.save(group);
        log.info("Created group {} for teacher {}", saved.getId(), teacher.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GroupResponse> list(UUID teacherId, Pageable pageable) {
        var page = (teacherId != null)
                ? groupRepository.findByTeacherId(teacherId, pageable)
                : groupRepository.findAll(pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupResponse get(UUID groupId, UUID callerId, boolean privileged) {
        StudyGroup group = requireGroup(groupId);
        assertManages(group, callerId, privileged);
        return toResponse(group);
    }

    @Override
    @Transactional
    public GroupResponse update(UUID groupId, GroupUpdate input) {
        StudyGroup group = requireGroup(groupId);
        if (input.name() != null && !input.name().isBlank()) {
            group.setName(input.name().trim());
        }
        if (input.teacherId() != null && !input.teacherId().equals(group.getTeacherId())) {
            Account teacher = requireAccount(input.teacherId());
            requireRole(teacher, ROLE_TEACHER, "Group owner must be a teacher");
            group.setTeacherId(teacher.getId());
        }
        return toResponse(groupRepository.save(group));
    }

    @Override
    @Transactional
    public void delete(UUID groupId) {
        StudyGroup group = requireGroup(groupId);
        // Memberships cascade-delete at the DB; remove explicitly for in-session consistency.
        membershipRepository.deleteAll(membershipRepository.findByGroupId(group.getId()));
        groupRepository.delete(group);
        log.info("Deleted group {}", groupId);
    }

    @Override
    @Transactional
    public MemberResponse addStudent(UUID groupId, UUID studentId, UUID callerId, boolean privileged) {
        StudyGroup group = requireGroup(groupId);
        assertManages(group, callerId, privileged);
        Account student = requireAccount(studentId);
        requireRole(student, ROLE_STUDENT, "Only students can be added to a group");
        if (membershipRepository.existsByStudentId(studentId)) {
            throw ApplicationException.conflict("Student already belongs to a group");
        }
        GroupMembership membership = new GroupMembership();
        membership.setGroupId(group.getId());
        membership.setStudentId(student.getId());
        membershipRepository.save(membership);
        log.info("Added student {} to group {}", studentId, groupId);
        return toMember(student, group.getId());
    }

    @Override
    @Transactional
    public void removeStudent(UUID groupId, UUID studentId, UUID callerId, boolean privileged) {
        StudyGroup group = requireGroup(groupId);
        assertManages(group, callerId, privileged);
        GroupMembership membership = membershipRepository.findByGroupIdAndStudentId(groupId, studentId)
                .orElseThrow(() -> ApplicationException.notFound("Student is not in this group"));
        membershipRepository.delete(membership);
        log.info("Removed student {} from group {}", studentId, groupId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> listMembers(UUID groupId, UUID callerId, boolean privileged) {
        StudyGroup group = requireGroup(groupId);
        assertManages(group, callerId, privileged);
        return toMembers(membershipRepository.findByGroupId(groupId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> studentsOf(UUID teacherId) {
        return toMembers(membershipRepository.findByTeacher(teacherId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isStudentOf(UUID teacherId, UUID studentId) {
        return membershipRepository.isStudentOf(teacherId, studentId);
    }

    // ---- helpers ----

    private GroupResponse toResponse(StudyGroup group) {
        String teacherUsername = accountRepository.findById(group.getTeacherId())
                .map(Account::getUsername).orElse(null);
        int count = (int) membershipRepository.countByGroupId(group.getId());
        return new GroupResponse(group.getId(), group.getName(), group.getTeacherId(),
                teacherUsername, count, group.getCreatedAt());
    }

    private List<MemberResponse> toMembers(List<GroupMembership> memberships) {
        if (memberships.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = memberships.stream().map(GroupMembership::getStudentId).toList();
        Map<UUID, Account> accounts = accountRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));
        return memberships.stream()
                .map(m -> {
                    Account a = accounts.get(m.getStudentId());
                    return new MemberResponse(m.getStudentId(),
                            a == null ? null : a.getUsername(),
                            a == null ? null : a.getEmail(),
                            m.getGroupId());
                })
                .toList();
    }

    private MemberResponse toMember(Account student, UUID groupId) {
        return new MemberResponse(student.getId(), student.getUsername(), student.getEmail(), groupId);
    }

    private Account requireAccount(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("Account not found"));
    }

    private StudyGroup requireGroup(UUID id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("Group not found"));
    }

    private void requireRole(Account account, String roleName, String message) {
        boolean has = account.getRoles().stream().map(Role::getName).anyMatch(roleName::equals);
        if (!has) {
            throw ApplicationException.badRequest(message);
        }
    }

    /** Admins/moderators manage any group; a teacher manages only their own. */
    private void assertManages(StudyGroup group, UUID callerId, boolean privileged) {
        if (!privileged && !group.getTeacherId().equals(callerId)) {
            throw ApplicationException.forbidden("You do not manage this group");
        }
    }
}
