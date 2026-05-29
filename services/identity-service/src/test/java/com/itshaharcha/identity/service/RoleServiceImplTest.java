package com.itshaharcha.identity.service;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.exception.ErrorCode;
import com.itshaharcha.identity.dto.request.RoleInput;
import com.itshaharcha.identity.dto.response.RoleResponse;
import com.itshaharcha.identity.entity.Role;
import com.itshaharcha.identity.mapper.RoleMapper;
import com.itshaharcha.identity.repository.RoleRepository;
import com.itshaharcha.identity.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock private RoleRepository roleRepository;
    @Mock private RoleMapper roleMapper;
    @InjectMocks private RoleServiceImpl service;

    @Test
    void list_returnsMappedRoles() {
        Role role = new Role("ROLE_ADMIN", "Admin");
        when(roleRepository.findAll(Sort.by("name"))).thenReturn(List.of(role));
        when(roleMapper.toResponse(role)).thenReturn(new RoleResponse("ROLE_ADMIN", "Admin"));

        List<RoleResponse> result = service.list();

        assertThat(result).extracting(RoleResponse::name).containsExactly("ROLE_ADMIN");
    }

    @Test
    void create_persistsNewRole() {
        var input = new RoleInput("ROLE_MENTOR", "Mentor");
        when(roleRepository.existsByName("ROLE_MENTOR")).thenReturn(false);
        when(roleRepository.save(any(Role.class)))
                .thenReturn(new Role("ROLE_MENTOR", "Mentor"));
        when(roleMapper.toResponse(any(Role.class)))
                .thenReturn(new RoleResponse("ROLE_MENTOR", "Mentor"));

        RoleResponse result = service.create(input);

        assertThat(result.name()).isEqualTo("ROLE_MENTOR");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void create_rejectsDuplicate() {
        var input = new RoleInput("ROLE_ADMIN", "Admin");
        when(roleRepository.existsByName("ROLE_ADMIN")).thenReturn(true);

        assertThatThrownBy(() -> service.create(input))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONFLICT);

        verify(roleRepository, never()).save(any());
    }

    @Test
    void delete_removesExistingRole() {
        Role role = new Role("ROLE_MENTOR", "Mentor");
        when(roleRepository.findByName("ROLE_MENTOR")).thenReturn(Optional.of(role));

        service.delete("ROLE_MENTOR");

        verify(roleRepository).delete(role);
    }

    @Test
    void delete_throwsWhenMissing() {
        when(roleRepository.findByName("ROLE_GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete("ROLE_GHOST"))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
    }
}
