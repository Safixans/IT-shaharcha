package com.itshaharcha.identity.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.identity.dto.request.LoginRequest;
import com.itshaharcha.identity.dto.request.RefreshRequest;
import com.itshaharcha.identity.dto.request.RegisterRequest;
import com.itshaharcha.identity.dto.request.ResendOtpRequest;
import com.itshaharcha.identity.dto.request.VerifyRequest;
import com.itshaharcha.identity.dto.response.AccountResponse;
import com.itshaharcha.identity.dto.response.TokenPair;
import com.itshaharcha.identity.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService authService;
    @InjectMocks private AuthController controller;

    @Test
    void register_delegatesAndWrapsMessage() {
        var req = new RegisterRequest("a@b.com", "jane", "Passw0rd!", "Jane");
        var account = mock(AccountResponse.class);
        when(authService.register(req)).thenReturn(account);

        ApiResponse<AccountResponse> res = controller.register(req);

        assertThat(res.success()).isTrue();
        assertThat(res.data()).isSameAs(account);
        assertThat(res.message()).contains("verification code");
        verify(authService).register(req);
    }

    @Test
    void login_delegates() {
        var req = new LoginRequest("jane", "Passw0rd!");
        var pair = mock(TokenPair.class);
        when(authService.login(req)).thenReturn(pair);

        assertThat(controller.login(req).data()).isSameAs(pair);
        verify(authService).login(req);
    }

    @Test
    void refresh_delegates() {
        var req = new RefreshRequest("token");
        var pair = mock(TokenPair.class);
        when(authService.refresh(req)).thenReturn(pair);

        assertThat(controller.refresh(req).data()).isSameAs(pair);
        verify(authService).refresh(req);
    }

    @Test
    void verify_delegatesAndReturnsMessage() {
        var req = new VerifyRequest("a@b.com", "1234");
        var account = mock(AccountResponse.class);
        when(authService.verify(req)).thenReturn(account);

        ApiResponse<AccountResponse> res = controller.verify(req);

        assertThat(res.data()).isSameAs(account);
        assertThat(res.message()).contains("verified");
        verify(authService).verify(req);
    }

    @Test
    void resendOtp_delegatesEmail() {
        ApiResponse<Void> res = controller.resendOtp(new ResendOtpRequest("a@b.com"));

        assertThat(res.success()).isTrue();
        verify(authService).resendOtp("a@b.com");
    }

    @Test
    void logout_delegatesRefreshToken() {
        controller.logout(new RefreshRequest("token"));

        verify(authService).logout("token");
    }

    @Test
    void logout_nullBody_passesNull() {
        controller.logout(null);

        verify(authService).logout(null);
    }
}
