package com.itshaharcha.identity.service.impl;

import com.itshaharcha.identity.config.AuthProperties;
import com.itshaharcha.identity.dto.request.LoginRequest;
import com.itshaharcha.identity.dto.request.RefreshRequest;
import com.itshaharcha.identity.dto.request.RegisterRequest;
import com.itshaharcha.identity.dto.request.VerifyRequest;
import com.itshaharcha.identity.dto.response.AccountResponse;
import com.itshaharcha.identity.dto.response.TokenPair;
import com.itshaharcha.identity.entity.Account;
import com.itshaharcha.identity.entity.AccountStatus;
import com.itshaharcha.identity.entity.AuthProvider;
import com.itshaharcha.identity.entity.OtpPurpose;
import com.itshaharcha.identity.entity.Profile;
import com.itshaharcha.identity.entity.RefreshToken;
import com.itshaharcha.identity.entity.Role;
import com.itshaharcha.identity.event.DomainEvent;
import com.itshaharcha.identity.kafka.EventPublisher;
import com.itshaharcha.identity.mapper.AccountMapper;
import com.itshaharcha.identity.repository.AccountRepository;
import com.itshaharcha.identity.repository.ProfileRepository;
import com.itshaharcha.identity.repository.RefreshTokenRepository;
import com.itshaharcha.identity.repository.RoleRepository;
import com.itshaharcha.identity.security.JwtTokenProvider;
import com.itshaharcha.identity.security.TokenType;
import com.itshaharcha.identity.service.AuthService;
import com.itshaharcha.identity.service.OtpService;
import com.itshaharcha.identity.util.HashUtil;
import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE = "ROLE_STUDENT";

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ProfileRepository profileRepository;
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

        Role studentRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new ApplicationException(ErrorCode.INTERNAL_ERROR,
                        "Default role " + DEFAULT_ROLE + " is not provisioned"));

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

        Profile profile = new Profile();
        profile.setAccountId(saved.getId());
        profile.setFullName(request.fullName());
        profileRepository.save(profile);

        if (!autoActivate) {
            otpService.issue(saved, OtpPurpose.EMAIL_VERIFICATION);
        }
        emit("identity.account.registered", saved,
                Map.of("email", saved.getEmail(), "username", saved.getUsername()));

        log.info("Registered account {} ({})", saved.getUsername(), saved.getId());
        return accountMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TokenPair login(LoginRequest request) {
        Account account = accountRepository
                .findByEmailIgnoreCaseOrUsernameIgnoreCase(request.identifier(), request.identifier())
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
        TokenPair pair = issueTokens(account);
        emit("identity.login.succeeded", account, Map.of());
        return pair;
    }

    @Override
    @Transactional
    public TokenPair refresh(RefreshRequest request) {
        var claims = tokenProvider.parse(request.refreshToken());
        tokenProvider.requireType(claims, TokenType.REFRESH);

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
    public AccountResponse verify(VerifyRequest request) {
        Account account = accountRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> ApplicationException.notFound("Account not found"));

        otpService.verifyAndConsume(account, OtpPurpose.EMAIL_VERIFICATION, request.code());
        account.setEmailVerified(true);
        account.setStatus(AccountStatus.ACTIVE);
        Account saved = accountRepository.save(account);
        emit("identity.account.verified", saved, Map.of());
        log.info("Verified account {}", saved.getId());
        return accountMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void resendOtp(String email) {
        // Do not leak account existence: only act if found and unverified.
        accountRepository.findByEmailIgnoreCase(email).ifPresent(account -> {
            if (!account.isEmailVerified()) {
                otpService.issue(account, OtpPurpose.EMAIL_VERIFICATION);
            }
        });
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(HashUtil.sha256(refreshToken)).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private TokenPair issueTokens(Account account) {
        Set<String> roleNames = account.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        String accessToken = tokenProvider.generateAccessToken(account, roleNames);
        String refreshToken = tokenProvider.generateRefreshToken(account);

        RefreshToken entity = new RefreshToken();
        entity.setAccount(account);
        entity.setTokenHash(HashUtil.sha256(refreshToken));
        entity.setExpiresAt(tokenProvider.refreshTokenExpiry());
        refreshTokenRepository.save(entity);

        return TokenPair.bearer(accessToken, refreshToken,
                tokenProvider.accessTokenTtlSeconds(), accountMapper.toResponse(account));
    }

    private void emit(String type, Account account, Map<String, Object> data) {
        Set<String> roles = account.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        DomainEvent event = DomainEvent.of(type, account.getId(), roles,
                new DomainEvent.Subject("account", account.getId().toString()), data);
        eventPublisher.publish(DomainEvent.TOPIC, account.getId().toString(), event);
    }
}
