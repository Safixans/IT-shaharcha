package com.itshaharcha.auth.controller;

import com.itshaharcha.auth.dto.request.LoginRequest;
import com.itshaharcha.auth.dto.request.RefreshTokenRequest;
import com.itshaharcha.auth.dto.request.RegisterRequest;
import com.itshaharcha.auth.dto.request.VerifyOtpRequest;
import com.itshaharcha.auth.dto.response.AccountResponse;
import com.itshaharcha.auth.dto.response.AuthResponse;
import com.itshaharcha.auth.service.AuthService;
import com.itshaharcha.common.web.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthService authService;
    @InjectMocks private AuthController controller;

    @Test
    void register_delegatesAndWrapsResponse() {
        var req = new RegisterRequest("a@b.com", "jane", "Passw0rd");
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
        var req = new LoginRequest("jane", "Passw0rd");
        var auth = mock(AuthResponse.class);
        when(authService.login(req)).thenReturn(auth);

        assertThat(controller.login(req).data()).isSameAs(auth);
        verify(authService).login(req);
    }

    @Test
    void refresh_delegates() {
        var req = new RefreshTokenRequest("token");
        var auth = mock(AuthResponse.class);
        when(authService.refresh(req)).thenReturn(auth);

        assertThat(controller.refresh(req).data()).isSameAs(auth);
        verify(authService).refresh(req);
    }

    @Test
    void verifyOtp_delegatesAndReturnsMessage() {
        var req = new VerifyOtpRequest("a@b.com", "123456");

        ApiResponse<Void> res = controller.verifyOtp(req);

        assertThat(res.success()).isTrue();
        assertThat(res.message()).contains("verified");
        verify(authService).verifyOtp(req);
    }

    @Test
    void resendOtp_delegatesAndReturnsMessage() {
        ApiResponse<Void> res = controller.resendOtp("a@b.com");

        assertThat(res.message()).contains("new verification code");
        verify(authService).resendOtp("a@b.com");
    }

    @Test
    void logout_delegatesRefreshToken() {
        var req = new RefreshTokenRequest("token");

        ApiResponse<Void> res = controller.logout(req);

        assertThat(res.message()).contains("Logged out");
        verify(authService).logout("token");
    }

    @Test
    void me_delegatesPrincipal() {
        UUID accountId = UUID.randomUUID();
        var account = mock(AccountResponse.class);
        when(authService.currentAccount(accountId)).thenReturn(account);

        assertThat(controller.me(accountId).data()).isSameAs(account);
        verify(authService).currentAccount(accountId);
    }

    private static <T> T mock(Class<T> type) {
        return org.mockito.Mockito.mock(type);
    }
}
