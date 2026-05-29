package com.itshaharcha.identity.controller;

import com.itshaharcha.identity.dto.request.SetRolesRequest;
import com.itshaharcha.identity.dto.request.SuspendRequest;
import com.itshaharcha.identity.dto.response.AccountResponse;
import com.itshaharcha.identity.dto.response.PageResponse;
import com.itshaharcha.identity.entity.AccountStatus;
import com.itshaharcha.identity.service.AccountAdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock private AccountAdminService accountAdminService;
    @InjectMocks private AccountController controller;

    @Test
    void list_delegatesFiltersAndPageable() {
        Pageable pageable = PageRequest.of(0, 20);
        @SuppressWarnings("unchecked")
        PageResponse<AccountResponse> page = mock(PageResponse.class);
        when(accountAdminService.list(AccountStatus.ACTIVE, "jane", pageable)).thenReturn(page);

        assertThat(controller.list(AccountStatus.ACTIVE, "jane", pageable).data()).isSameAs(page);
        verify(accountAdminService).list(AccountStatus.ACTIVE, "jane", pageable);
    }

    @Test
    void get_delegates() {
        UUID id = UUID.randomUUID();
        var account = mock(AccountResponse.class);
        when(accountAdminService.get(id)).thenReturn(account);

        assertThat(controller.get(id).data()).isSameAs(account);
        verify(accountAdminService).get(id);
    }

    @Test
    void suspend_passesReasonFromBody() {
        UUID id = UUID.randomUUID();
        var account = mock(AccountResponse.class);
        when(accountAdminService.suspend(id, "spam")).thenReturn(account);

        assertThat(controller.suspend(id, new SuspendRequest("spam")).data()).isSameAs(account);
        verify(accountAdminService).suspend(id, "spam");
    }

    @Test
    void suspend_nullBody_passesNullReason() {
        UUID id = UUID.randomUUID();
        var account = mock(AccountResponse.class);
        when(accountAdminService.suspend(id, null)).thenReturn(account);

        assertThat(controller.suspend(id, null).data()).isSameAs(account);
        verify(accountAdminService).suspend(id, null);
    }

    @Test
    void activate_delegates() {
        UUID id = UUID.randomUUID();
        var account = mock(AccountResponse.class);
        when(accountAdminService.activate(id)).thenReturn(account);

        assertThat(controller.activate(id).data()).isSameAs(account);
        verify(accountAdminService).activate(id);
    }

    @Test
    void setRoles_passesRoleList() {
        UUID id = UUID.randomUUID();
        var account = mock(AccountResponse.class);
        when(accountAdminService.setRoles(id, List.of("ROLE_ADMIN"))).thenReturn(account);

        assertThat(controller.setRoles(id, new SetRolesRequest(List.of("ROLE_ADMIN"))).data())
                .isSameAs(account);
        verify(accountAdminService).setRoles(id, List.of("ROLE_ADMIN"));
    }
}
