package com.itshaharcha.identity.service;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.exception.ErrorCode;
import com.itshaharcha.identity.dto.response.DomainAnalyticsSummary;
import com.itshaharcha.identity.dto.response.MetricSeriesSet;
import com.itshaharcha.identity.dto.response.PageResponse;
import com.itshaharcha.identity.entity.Account;
import com.itshaharcha.identity.entity.Role;
import com.itshaharcha.identity.event.DomainEvent;
import com.itshaharcha.identity.repository.AccountRepository;
import com.itshaharcha.identity.service.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock private AccountRepository accountRepository;
    @InjectMocks private AnalyticsServiceImpl service;

    private UUID accountId;
    private Account account;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        account = new Account();
        account.setId(accountId);
        account.setEmailVerified(true);
        account.addRole(new Role("ROLE_STUDENT", "Student"));
    }

    @Test
    void summary_isDerivedFromAccountState() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        DomainAnalyticsSummary summary = service.summary(accountId, from, to);

        assertThat(summary.accountId()).isEqualTo(accountId);
        assertThat(summary.domain()).isEqualTo("identity");
        assertThat(summary.windowFrom()).isEqualTo(from);
        assertThat(summary.windowTo()).isEqualTo(to);
        assertThat(summary.counters()).containsEntry("roles", 1).containsEntry("emailVerified", 1);
    }

    @Test
    void activity_returnsEmptyWellFormedPage() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        PageResponse<DomainEvent> page = service.activity(
                accountId, null, null, PageRequest.of(2, 25));

        assertThat(page.items()).isEmpty();
        assertThat(page.meta().page()).isEqualTo(2);
        assertThat(page.meta().size()).isEqualTo(25);
        assertThat(page.meta().totalElements()).isZero();
        assertThat(page.meta().hasNext()).isFalse();
    }

    @Test
    void metrics_returnsEmptySeriesSet() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        MetricSeriesSet set = service.metrics(accountId, null, "day", null, null);

        assertThat(set.accountId()).isEqualTo(accountId);
        assertThat(set.granularity()).isEqualTo("day");
        assertThat(set.series()).isEmpty();
    }

    @Test
    void summary_throwsWhenAccountMissing() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.summary(accountId, null, null))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
    }
}
