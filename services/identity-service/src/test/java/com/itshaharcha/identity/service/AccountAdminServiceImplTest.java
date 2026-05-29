package com.itshaharcha.identity.service;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.exception.ErrorCode;
import com.itshaharcha.identity.dto.response.AccountResponse;
import com.itshaharcha.identity.dto.response.PageResponse;
import com.itshaharcha.identity.entity.Account;
import com.itshaharcha.identity.entity.AccountStatus;
import com.itshaharcha.identity.entity.AuthProvider;
import com.itshaharcha.identity.entity.Role;
import com.itshaharcha.identity.kafka.EventPublisher;
import com.itshaharcha.identity.mapper.AccountMapper;
import com.itshaharcha.identity.repository.AccountRepository;
import com.itshaharcha.identity.repository.RoleRepository;
import com.itshaharcha.identity.service.impl.AccountAdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountAdminServiceImplTest {

    @Mock private AccountRepository accountRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private AccountMapper accountMapper;
    @Mock private EventPublisher eventPublisher;
    @InjectMocks private AccountAdminServiceImpl service;

    private UUID accountId;
    private Account account;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        account = new Account();
        account.setId(accountId);
        account.setUsername("jane");
        account.setStatus(AccountStatus.ACTIVE);
        account.addRole(new Role("ROLE_STUDENT", "Student"));
    }

    private AccountResponse mapped(AccountStatus status) {
        return new AccountResponse(accountId, "jane@example.com", "jane", status,
                true, AuthProvider.LOCAL, List.of("ROLE_STUDENT"), Instant.now(), Instant.now());
    }

    @Test
    void list_blankQuery_isNormalizedToNull() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Account> page = new PageImpl<>(List.of(account), pageable, 1);
        when(accountRepository.search(eq(AccountStatus.ACTIVE), isNull(), eq(pageable))).thenReturn(page);
        when(accountMapper.toResponse(account)).thenReturn(mapped(AccountStatus.ACTIVE));

        PageResponse<AccountResponse> result = service.list(AccountStatus.ACTIVE, "   ", pageable);

        assertThat(result.items()).hasSize(1);
        assertThat(result.meta().totalElements()).isEqualTo(1);
    }

    @Test
    void get_returnsMappedAccount() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountMapper.toResponse(account)).thenReturn(mapped(AccountStatus.ACTIVE));

        assertThat(service.get(accountId).username()).isEqualTo("jane");
    }

    @Test
    void get_throwsWhenMissing() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(accountId))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    void suspend_setsStatus_andEmitsEvent() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(mapped(AccountStatus.SUSPENDED));

        service.suspend(accountId, "policy violation");

        assertThat(account.getStatus()).isEqualTo(AccountStatus.SUSPENDED);
        verify(eventPublisher).publish(eq("itsh.identity.events"), any(), any());
    }

    @Test
    void activate_setsStatus_andEmitsEvent() {
        account.setStatus(AccountStatus.SUSPENDED);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(mapped(AccountStatus.ACTIVE));

        service.activate(accountId);

        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        verify(eventPublisher).publish(eq("itsh.identity.events"), any(), any());
    }

    @Test
    void setRoles_resolvesNames_andReplaces() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(roleRepository.findByName("ROLE_ADMIN"))
                .thenReturn(Optional.of(new Role("ROLE_ADMIN", "Admin")));
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(mapped(AccountStatus.ACTIVE));

        service.setRoles(accountId, List.of("ROLE_ADMIN"));

        assertThat(account.getRoles()).extracting(Role::getName).containsExactly("ROLE_ADMIN");
    }

    @Test
    void setRoles_rejectsUnknownRole() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(roleRepository.findByName("ROLE_GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.setRoles(accountId, List.of("ROLE_GHOST")))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.BAD_REQUEST);

        verify(accountRepository, never()).save(any());
    }
}
