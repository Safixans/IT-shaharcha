package com.itshaharcha.identity.controller;

import com.itshaharcha.identity.dto.request.RoleInput;
import com.itshaharcha.identity.dto.response.RoleResponse;
import com.itshaharcha.identity.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock private RoleService roleService;
    @InjectMocks private RoleController controller;

    @Test
    void list_delegates() {
        var role = new RoleResponse("ROLE_ADMIN", "Admin");
        when(roleService.list()).thenReturn(List.of(role));

        assertThat(controller.list().data()).containsExactly(role);
        verify(roleService).list();
    }

    @Test
    void create_delegates() {
        var input = new RoleInput("ROLE_MENTOR", "Mentor");
        var response = new RoleResponse("ROLE_MENTOR", "Mentor");
        when(roleService.create(input)).thenReturn(response);

        assertThat(controller.create(input).data()).isSameAs(response);
        verify(roleService).create(input);
    }

    @Test
    void delete_delegatesByName() {
        controller.delete("ROLE_MENTOR");

        verify(roleService).delete("ROLE_MENTOR");
    }
}
