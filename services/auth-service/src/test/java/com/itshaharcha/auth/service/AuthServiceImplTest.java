package com.itshaharcha.auth.service;

import com.itshaharcha.auth.config.AuthProperties;
import com.itshaharcha.auth.dto.request.LoginRequest;
import com.itshaharcha.auth.dto.request.RefreshTokenRequest;
import com.itshaharcha.auth.dto.request.RegisterRequest;
import com.itshaharcha.auth.dto.request.VerifyOtpRequest;
import com.itshaharcha.auth.dto.response.AccountResponse;
import com.itshaharcha.auth.dto.response.AuthResponse;
import com.itshaharcha.auth.entity.Account;
import com.itshaharcha.auth.entity.AccountStatus;
import com.itshaharcha.auth.entity.OtpPurpose;
import com.itshaharcha.auth.entity.RefreshToken;
import com.itshaharcha.auth.entity.Role;
import com.itshaharcha.auth.entity.RoleName;
import com.itshaharcha.auth.kafka.EventPublisher;
import com.itshaharcha.auth.mapper.AccountMapper;
import com.itshaharcha.auth.repository.AccountRepository;
import com.itshaharcha.auth.repository.RefreshTokenRepository;
import com.itshaharcha.auth.repository.RoleRepository;
import com.itshaharcha.auth.security.JwtTokenProvider;
import com.itshaharcha.auth.security.TokenType;
import com.itshaharcha.auth.service.impl.AuthServiceImpl;
import com.itshaharcha.auth.util.HashUtil;
import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AccountRepository accountRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private OtpService otpService;
    @Mock private EventPublisher eventPublisher;
    @Mock private AccountMapper accountMapper;
    @Mock private AuthProperties authProperties;

    @InjectMocks private AuthServiceImpl authService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setId(UUID.randomUUID());
        account.setEmail("jane@example.com");
        account.setUsername("jane");
        account.setPasswordHash("hashed");
        account.setStatus(AccountStatus.ACTIVE);
        account.addRole(new Role(RoleName.ROLE_STUDENT));
    }

    private void stubSaveReturnsArgWithId() {
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            if (a.getId() == null) {
                a.setId(UUID.randomUUID());
            }
            return a;
        });
    }

    private void stubTokenIssuance() {
        when(tokenProvider.generateAccessToken(any(Account.class), anySet())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(Account.class))).thenReturn("refresh-token");
        when(tokenProvider.refreshTokenExpiry()).thenReturn(Instant.now().plusSeconds(3600));
        when(tokenProvider.accessTokenTtlSeconds()).thenReturn(900L);
    }

    // ---- register --------------------------------------------------------

    @Test
    void register_whenNotAutoActivate_issuesOtp_setsPending_publishesEvent() {
        var request = new RegisterRequest("Jane@Example.com", "jane", "Password1");
        when(accountRepository.existsByEmailIgnoreCase("Jane@Example.com")).thenReturn(false);
        when(accountRepository.existsByUsernameIgnoreCase("jane")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_STUDENT))
                .thenReturn(Optional.of(new Role(RoleName.ROLE_STUDENT)));
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(authProperties.autoActivate()).thenReturn(false);
        stubSaveReturnsArgWithId();
        when(accountMapper.toResponse(any(Account.class))).thenReturn(
                new AccountResponse(UUID.randomUUID(), "jane@example.com", "jane",
                        AccountStatus.PENDING, false, null, Set.of("ROLE_STUDENT")));

        AccountResponse response = authService.register(request);

        assertThat(response.username()).isEqualTo("jane");
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account persisted = captor.getValue();
        assertThat(persisted.getEmail()).isEqualTo("jane@example.com"); // lowercased
        assertThat(persisted.getStatus()).isEqualTo(AccountStatus.PENDING);
        assertThat(persisted.isEmailVerified()).isFalse();
        verify(otpService).issue(any(Account.class), eq(OtpPurpose.EMAIL_VERIFICATION));
        verify(eventPublisher).publish(eq("user.registered"), any(), any());
    }

    @Test
    void register_whenAutoActivate_skipsOtp_setsActiveAndVerified() {
        var request = new RegisterRequest("jane@example.com", "jane", "Password1");
        when(accountRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(false);
        when(accountRepository.existsByUsernameIgnoreCase("jane")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_STUDENT))
                .thenReturn(Optional.of(new Role(RoleName.ROLE_STUDENT)));
        when(passwordEncoder.encode("Password1")).thenReturn("hashed");
        when(authProperties.autoActivate()).thenReturn(true);
        stubSaveReturnsArgWithId();
        when(accountMapper.toResponse(any(Account.class))).thenReturn(
                new AccountResponse(UUID.randomUUID(), "jane@example.com", "jane",
                        AccountStatus.ACTIVE, true, null, Set.of("ROLE_STUDENT")));

        authService.register(request);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(captor.getValue().isEmailVerified()).isTrue();
        verify(otpService, never()).issue(any(), any());
        verify(eventPublisher).publish(eq("user.registered"), any(), any());
    }

    @Test
    void register_rejectsDuplicateEmail() {
        var request = new RegisterRequest("jane@example.com", "jane", "Password1");
        when(accountRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONFLICT);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_rejectsDuplicateUsername() {
        var request = new RegisterRequest("jane@example.com", "jane", "Password1");
        when(accountRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(false);
        when(accountRepository.existsByUsernameIgnoreCase("jane")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CONFLICT);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_whenDefaultRoleMissing_throwsInternalError() {
        var request = new RegisterRequest("jane@example.com", "jane", "Password1");
        when(accountRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(false);
        when(accountRepository.existsByUsernameIgnoreCase("jane")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_STUDENT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INTERNAL_ERROR);

        verify(accountRepository, never()).save(any());
    }

    // ---- login -----------------------------------------------------------

    @Test
    void login_returnsTokens_whenCredentialsValid() {
        var request = new LoginRequest("jane", "Password1");
        when(accountRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase("jane", "jane"))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches("Password1", "hashed")).thenReturn(true);
        stubTokenIssuance();
        when(accountMapper.toResponse(account)).thenReturn(
                new AccountResponse(account.getId(), "jane@example.com", "jane",
                        AccountStatus.ACTIVE, true, null, Set.of("ROLE_STUDENT")));

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(900L);
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void login_rejectsUnknownAccount() {
        var request = new LoginRequest("ghost", "x");
        when(accountRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase("ghost", "ghost"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void login_rejectsNullPasswordHash() {
        account.setPasswordHash(null);
        var request = new LoginRequest("jane", "Password1");
        when(accountRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase("jane", "jane"))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void login_rejectsInvalidPassword() {
        var request = new LoginRequest("jane", "wrong");
        when(accountRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase("jane", "jane"))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_CREDENTIALS);
    }

    @Test
    void login_rejectsPendingAccount() {
        account.setStatus(AccountStatus.PENDING);
        var request = new LoginRequest("jane", "Password1");
        when(accountRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase("jane", "jane"))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches("Password1", "hashed")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    void login_rejectsSuspendedAccount() {
        account.setStatus(AccountStatus.SUSPENDED);
        var request = new LoginRequest("jane", "Password1");
        when(accountRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase("jane", "jane"))
                .thenReturn(Optional.of(account));
        when(passwordEncoder.matches("Password1", "hashed")).thenReturn(true);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.FORBIDDEN);
    }

    // ---- refresh ---------------------------------------------------------

    @Test
    void refresh_rotatesToken_andIssuesNewPair() {
        var request = new RefreshTokenRequest("raw-refresh");
        Claims claims = mock(Claims.class);
        when(tokenProvider.parse("raw-refresh")).thenReturn(claims);

        RefreshToken stored = new RefreshToken();
        stored.setAccount(account);
        stored.setExpiresAt(Instant.now().plusSeconds(3600));
        stored.setRevoked(false);
        when(refreshTokenRepository.findByTokenHash(HashUtil.sha256("raw-refresh")))
                .thenReturn(Optional.of(stored));
        stubTokenIssuance();
        when(accountMapper.toResponse(account)).thenReturn(
                new AccountResponse(account.getId(), "jane@example.com", "jane",
                        AccountStatus.ACTIVE, true, null, Set.of("ROLE_STUDENT")));

        AuthResponse response = authService.refresh(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(stored.isRevoked()).isTrue();
        verify(tokenProvider).requireType(claims, TokenType.REFRESH);
        verify(refreshTokenRepository).save(stored); // revoke
    }

    @Test
    void refresh_rejectsUnknownToken() {
        var request = new RefreshTokenRequest("raw-refresh");
        when(tokenProvider.parse("raw-refresh")).thenReturn(mock(Claims.class));
        when(refreshTokenRepository.findByTokenHash(HashUtil.sha256("raw-refresh")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TOKEN_INVALID);
    }

    @Test
    void refresh_rejectsRevokedToken() {
        var request = new RefreshTokenRequest("raw-refresh");
        when(tokenProvider.parse("raw-refresh")).thenReturn(mock(Claims.class));
        RefreshToken stored = new RefreshToken();
        stored.setAccount(account);
        stored.setExpiresAt(Instant.now().plusSeconds(3600));
        stored.setRevoked(true);
        when(refreshTokenRepository.findByTokenHash(HashUtil.sha256("raw-refresh")))
                .thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.TOKEN_INVALID);
    }

    // ---- verifyOtp -------------------------------------------------------

    @Test
    void verifyOtp_activatesAccount() {
        account.setStatus(AccountStatus.PENDING);
        account.setEmailVerified(false);
        var request = new VerifyOtpRequest("jane@example.com", "123456");
        when(accountRepository.findByEmailIgnoreCase("jane@example.com"))
                .thenReturn(Optional.of(account));

        authService.verifyOtp(request);

        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.isEmailVerified()).isTrue();
        verify(otpService).verifyAndConsume(account, OtpPurpose.EMAIL_VERIFICATION, "123456");
        verify(accountRepository).save(account);
    }

    @Test
    void verifyOtp_rejectsUnknownAccount() {
        var request = new VerifyOtpRequest("ghost@example.com", "123456");
        when(accountRepository.findByEmailIgnoreCase("ghost@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyOtp(request))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
    }

    // ---- resendOtp -------------------------------------------------------

    @Test
    void resendOtp_issuesNewCode() {
        account.setEmailVerified(false);
        when(accountRepository.findByEmailIgnoreCase("jane@example.com"))
                .thenReturn(Optional.of(account));

        authService.resendOtp("jane@example.com");

        verify(otpService).issue(account, OtpPurpose.EMAIL_VERIFICATION);
    }

    @Test
    void resendOtp_rejectsAlreadyVerified() {
        account.setEmailVerified(true);
        when(accountRepository.findByEmailIgnoreCase("jane@example.com"))
                .thenReturn(Optional.of(account));

        assertThatThrownBy(() -> authService.resendOtp("jane@example.com"))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.BAD_REQUEST);

        verify(otpService, never()).issue(any(), any());
    }

    @Test
    void resendOtp_rejectsUnknownAccount() {
        when(accountRepository.findByEmailIgnoreCase("ghost@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resendOtp("ghost@example.com"))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
    }

    // ---- logout ----------------------------------------------------------

    @Test
    void logout_revokesStoredToken() {
        RefreshToken stored = new RefreshToken();
        stored.setRevoked(false);
        when(refreshTokenRepository.findByTokenHash(HashUtil.sha256("raw")))
                .thenReturn(Optional.of(stored));

        authService.logout("raw");

        assertThat(stored.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    void logout_isNoOp_whenTokenUnknown() {
        when(refreshTokenRepository.findByTokenHash(HashUtil.sha256("raw")))
                .thenReturn(Optional.empty());

        authService.logout("raw");

        verify(refreshTokenRepository, never()).save(any());
    }

    // ---- currentAccount --------------------------------------------------

    @Test
    void currentAccount_returnsMappedAccount() {
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
        when(accountMapper.toResponse(account)).thenReturn(
                new AccountResponse(account.getId(), "jane@example.com", "jane",
                        AccountStatus.ACTIVE, true, null, Set.of("ROLE_STUDENT")));

        AccountResponse response = authService.currentAccount(account.getId());

        assertThat(response.username()).isEqualTo("jane");
    }

    @Test
    void currentAccount_throwsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(accountRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.currentAccount(id))
                .isInstanceOf(ApplicationException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOT_FOUND);
    }
}
