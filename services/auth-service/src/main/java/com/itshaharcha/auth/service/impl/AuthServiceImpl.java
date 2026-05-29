package com.itshaharcha.auth.service.impl;

import com.itshaharcha.auth.dto.request.LoginRequest;
import com.itshaharcha.auth.dto.request.RefreshTokenRequest;
import com.itshaharcha.auth.dto.request.RegisterRequest;
import com.itshaharcha.auth.dto.request.VerifyOtpRequest;
import com.itshaharcha.auth.dto.response.AccountResponse;
import com.itshaharcha.auth.dto.response.AuthResponse;
import com.itshaharcha.auth.entity.Account;
import com.itshaharcha.auth.entity.AccountStatus;
import com.itshaharcha.auth.entity.AuthProvider;
import com.itshaharcha.auth.entity.OtpPurpose;
import com.itshaharcha.auth.entity.RefreshToken;
import com.itshaharcha.auth.entity.Role;
import com.itshaharcha.auth.entity.RoleName;
import com.itshaharcha.auth.config.AuthProperties;
import com.itshaharcha.auth.event.UserRegisteredEvent;
import com.itshaharcha.auth.kafka.EventPublisher;
import com.itshaharcha.auth.mapper.AccountMapper;
import com.itshaharcha.auth.repository.AccountRepository;
import com.itshaharcha.auth.repository.RefreshTokenRepository;
import com.itshaharcha.auth.repository.RoleRepository;
import com.itshaharcha.auth.security.JwtTokenProvider;
import com.itshaharcha.auth.service.AuthService;
import com.itshaharcha.auth.service.OtpService;
import com.itshaharcha.auth.util.HashUtil;
import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final OtpService otpService;
    private final EventPublisher eventPublisher;
    private final AccountMapper accountMapper;
    private final AuthProperties authProperties;

    @Override
    @Transactional
    public AccountResponse register(RegisterRequest request) {
        if (accountRepository.existsByEmailIgnoreCase(request.email())) {
            throw ApplicationException.conflict("Email is already registered");
        }
        if (accountRepository.existsByUsernameIgnoreCase(request.username())) {
            throw ApplicationException.conflict("Username is already taken");
        }

        Role studentRole = roleRepository.findByName(RoleName.ROLE_STUDENT)
                .orElseThrow(() -> new ApplicationException(ErrorCode.INTERNAL_ERROR,
                        "Default role ROLE_STUDENT is not provisioned"));

        Account account = new Account();
        account.setEmail(request.email().toLowerCase());
        account.setUsername(request.username());
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setProvider(AuthProvider.LOCAL);
        account.addRole(studentRole);

        boolean autoActivate = authProperties.autoActivate();
        account.setStatus(autoActivate ? AccountStatus.ACTIVE : AccountStatus.PENDING);
        account.setEmailVerified(autoActivate);
        Account saved = accountRepository.save(account);

        if (!autoActivate) {
            otpService.issue(saved, OtpPurpose.EMAIL_VERIFICATION);
        }
        eventPublisher.publish(UserRegisteredEvent.TOPIC, saved.getId().toString(),
                new UserRegisteredEvent(saved.getId(), saved.getEmail(), saved.getUsername(),
                        Instant.now()));

        log.info("Registered account {} ({})", saved.getUsername(), saved.getId());
        return accountMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Account account = accountRepository
                .findByEmailIgnoreCaseOrUsernameIgnoreCase(
                        request.usernameOrEmail(), request.usernameOrEmail())
                .orElseThrow(() -> new ApplicationException(
                        ErrorCode.INVALID_CREDENTIALS, "Invalid credentials"));

        if (account.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new ApplicationException(ErrorCode.INVALID_CREDENTIALS, "Invalid credentials");
        }
        if (account.getStatus() == AccountStatus.PENDING) {
            throw new ApplicationException(ErrorCode.FORBIDDEN,
                    "Account is not verified. Please confirm the code sent to your email.");
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ApplicationException(ErrorCode.FORBIDDEN, "Account is not active");
        }
        return issueTokens(account);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        var claims = tokenProvider.parse(request.refreshToken());
        tokenProvider.requireType(claims, com.itshaharcha.auth.security.TokenType.REFRESH);

        String tokenHash = HashUtil.sha256(request.refreshToken());
        RefreshToken stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ApplicationException(
                        ErrorCode.TOKEN_INVALID, "Refresh token not recognized"));

        if (!stored.isActive()) {
            throw new ApplicationException(ErrorCode.TOKEN_INVALID, "Refresh token is no longer valid");
        }

        // Rotation: revoke the presented token before issuing a new pair.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return issueTokens(stored.getAccount());
    }

    @Override
    @Transactional
    public void verifyOtp(VerifyOtpRequest request) {
        Account account = accountRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> ApplicationException.notFound("Account not found"));

        otpService.verifyAndConsume(account, OtpPurpose.EMAIL_VERIFICATION, request.code());
        account.setEmailVerified(true);
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
        log.info("Verified account {}", account.getId());
    }

    @Override
    @Transactional
    public void resendOtp(String email) {
        Account account = accountRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> ApplicationException.notFound("Account not found"));
        if (account.isEmailVerified()) {
            throw ApplicationException.badRequest("Account is already verified");
        }
        otpService.issue(account, OtpPurpose.EMAIL_VERIFICATION);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenHash(HashUtil.sha256(refreshToken)).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse currentAccount(UUID accountId) {
        return accountRepository.findById(accountId)
                .map(accountMapper::toResponse)
                .orElseThrow(() -> ApplicationException.notFound("Account not found"));
    }

    private AuthResponse issueTokens(Account account) {
        Set<String> roleNames = account.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        String accessToken = tokenProvider.generateAccessToken(account, roleNames);
        String refreshToken = tokenProvider.generateRefreshToken(account);

        RefreshToken entity = new RefreshToken();
        entity.setAccount(account);
        entity.setTokenHash(HashUtil.sha256(refreshToken));
        entity.setExpiresAt(tokenProvider.refreshTokenExpiry());
        refreshTokenRepository.save(entity);

        return AuthResponse.bearer(accessToken, refreshToken,
                tokenProvider.accessTokenTtlSeconds(), accountMapper.toResponse(account));
    }
}
