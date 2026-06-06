package com.itshaharcha.identity.service;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.identity.dto.request.GroupInput;
import com.itshaharcha.identity.entity.Account;
import com.itshaharcha.identity.entity.Role;
import com.itshaharcha.identity.entity.StudyGroup;
import com.itshaharcha.identity.repository.AccountRepository;
import com.itshaharcha.identity.repository.GroupMembershipRepository;
import com.itshaharcha.identity.repository.GroupRepository;
import com.itshaharcha.identity.service.impl.GroupServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock private GroupRepository groupRepository;
    @Mock private GroupMembershipRepository membershipRepository;
    @Mock private AccountRepository accountRepository;
    @InjectMocks private GroupServiceImpl service;

    private Account account(UUID id, String... roles) {
        Account a = new Account();
        a.setId(id);
        a.setUsername("user-" + id.toString().substring(0, 4));
        a.setEmail(id + "@x.io");
        Set<Role> set = new java.util.HashSet<>();
        for (String r : roles) set.add(new Role(r, r));
        a.setRoles(set);
        return a;
    }

    @Test
    void create_requiresTeacherRole() {
        UUID teacherId = UUID.randomUUID();
        when(accountRepository.findById(teacherId)).thenReturn(Optional.of(account(teacherId, "ROLE_STUDENT")));

        assertThatThrownBy(() -> service.create(new GroupInput("A", teacherId)))
                .isInstanceOf(ApplicationException.class);
        verify(groupRepository, never()).save(any());
    }

    @Test
    void create_savesGroupForTeacher() {
        UUID teacherId = UUID.randomUUID();
        when(accountRepository.findById(teacherId)).thenReturn(Optional.of(account(teacherId, "ROLE_TEACHER")));
        when(groupRepository.save(any(StudyGroup.class))).thenAnswer(i -> {
            StudyGroup g = i.getArgument(0);
            g.setId(UUID.randomUUID());
            return g;
        });
        when(membershipRepository.countByGroupId(any())).thenReturn(0L);

        var res = service.create(new GroupInput("Group A", teacherId));

        assertThat(res.name()).isEqualTo("Group A");
        assertThat(res.teacherId()).isEqualTo(teacherId);
        verify(groupRepository).save(any(StudyGroup.class));
    }

    @Test
    void addStudent_rejectsStudentAlreadyInAGroup() {
        UUID groupId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        StudyGroup group = new StudyGroup();
        group.setId(groupId);
        group.setTeacherId(teacherId);
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(accountRepository.findById(studentId)).thenReturn(Optional.of(account(studentId, "ROLE_STUDENT")));
        when(membershipRepository.existsByStudentId(studentId)).thenReturn(true);

        assertThatThrownBy(() -> service.addStudent(groupId, studentId, teacherId, false))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("already belongs");
        verify(membershipRepository, never()).save(any());
    }

    @Test
    void addStudent_forbiddenWhenTeacherNotOwner() {
        UUID groupId = UUID.randomUUID();
        StudyGroup group = new StudyGroup();
        group.setId(groupId);
        group.setTeacherId(UUID.randomUUID()); // owned by someone else
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> service.addStudent(groupId, UUID.randomUUID(), UUID.randomUUID(), false))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("do not manage");
    }

    @Test
    void addStudent_succeedsForOwningTeacher() {
        UUID groupId = UUID.randomUUID();
        UUID teacherId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        StudyGroup group = new StudyGroup();
        group.setId(groupId);
        group.setTeacherId(teacherId);
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(accountRepository.findById(studentId)).thenReturn(Optional.of(account(studentId, "ROLE_STUDENT")));
        when(membershipRepository.existsByStudentId(studentId)).thenReturn(false);
        lenient().when(membershipRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var res = service.addStudent(groupId, studentId, teacherId, false);

        assertThat(res.studentId()).isEqualTo(studentId);
        assertThat(res.groupId()).isEqualTo(groupId);
        verify(membershipRepository).save(any());
    }

    @Test
    void isStudentOf_delegatesToRepository() {
        UUID t = UUID.randomUUID();
        UUID s = UUID.randomUUID();
        when(membershipRepository.isStudentOf(t, s)).thenReturn(true);
        assertThat(service.isStudentOf(t, s)).isTrue();
    }
}
