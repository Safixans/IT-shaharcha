package com.itshaharcha.identity.mapper;

import com.itshaharcha.identity.dto.response.AccountResponse;
import com.itshaharcha.identity.entity.Account;
import com.itshaharcha.identity.entity.AccountStatus;
import com.itshaharcha.identity.entity.AuthProvider;
import com.itshaharcha.identity.entity.Role;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountMapperTest {

    private final AccountMapper mapper = new AccountMapperImpl();

    @Test
    void toResponse_mapsAllFieldsAndSortsRoleNames() {
        Account account = new Account();
        UUID id = UUID.randomUUID();
        account.setId(id);
        account.setEmail("jane@example.com");
        account.setUsername("jane");
        account.setStatus(AccountStatus.ACTIVE);
        account.setEmailVerified(true);
        account.setProvider(AuthProvider.LOCAL);
        account.addRole(new Role("ROLE_TEACHER", "Teacher"));
        account.addRole(new Role("ROLE_STUDENT", "Student"));

        AccountResponse response = mapper.toResponse(account);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.email()).isEqualTo("jane@example.com");
        assertThat(response.username()).isEqualTo("jane");
        assertThat(response.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(response.emailVerified()).isTrue();
        assertThat(response.provider()).isEqualTo(AuthProvider.LOCAL);
        assertThat(response.roles()).containsExactly("ROLE_STUDENT", "ROLE_TEACHER"); // sorted
    }

    @Test
    void rolesToNames_handlesEmptySet() {
        assertThat(mapper.rolesToNames(Set.of())).isEmpty();
    }

    @Test
    void toResponse_nullAccount_returnsNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }
}
