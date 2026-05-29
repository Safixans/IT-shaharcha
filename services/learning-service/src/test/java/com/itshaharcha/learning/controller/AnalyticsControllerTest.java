package com.itshaharcha.learning.controller;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.exception.ErrorCode;
import com.itshaharcha.learning.dto.response.DomainAnalyticsSummary;
import com.itshaharcha.learning.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock private AnalyticsService analyticsService;
    @InjectMocks private AnalyticsController controller;

    private Authentication auth(UUID self, String... authorities) {
        return new UsernamePasswordAuthenticationToken(self, null,
                List.of(authorities).stream().map(SimpleGrantedAuthority::new).toList());
    }

    @Test
    void summary_defaultsToSelf() {
        UUID self = UUID.randomUUID();
        var result = mock(DomainAnalyticsSummary.class);
        when(analyticsService.summary(self, null, null)).thenReturn(result);

        assertThat(controller.summary(null, null, null, auth(self, "ROLE_STUDENT")).data())
                .isSameAs(result);
        verify(analyticsService).summary(self, null, null);
    }

    @Test
    void summary_adminMayReadAnotherAccount() {
        UUID self = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        var result = mock(DomainAnalyticsSummary.class);
        when(analyticsService.summary(other, null, null)).thenReturn(result);

        assertThat(controller.summary(other, null, null, auth(self, "ROLE_ADMIN")).data())
                .isSameAs(result);
    }

    @Test
    void summary_nonAdminCannotReadAnotherAccount() {
        UUID self = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        assertThatThrownBy(() -> controller.summary(other, null, null, auth(self, "ROLE_STUDENT")))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.FORBIDDEN);

        verifyNoInteractions(analyticsService);
    }
}
