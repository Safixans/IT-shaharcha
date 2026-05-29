package com.itshaharcha.identity.service.impl;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.identity.dto.response.AccountResponse;
import com.itshaharcha.identity.dto.response.PageResponse;
import com.itshaharcha.identity.entity.Account;
import com.itshaharcha.identity.entity.AccountStatus;
import com.itshaharcha.identity.entity.Role;
import com.itshaharcha.identity.event.DomainEvent;
import com.itshaharcha.identity.kafka.EventPublisher;
import com.itshaharcha.identity.mapper.AccountMapper;
import com.itshaharcha.identity.repository.AccountRepository;
import com.itshaharcha.identity.repository.RoleRepository;
import com.itshaharcha.identity.service.AccountAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountAdminServiceImpl implements AccountAdminService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final AccountMapper accountMapper;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AccountResponse> list(AccountStatus status, String q, Pageable pageable) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        return PageResponse.from(
                accountRepository.search(status, query, pageable), accountMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse get(UUID accountId) {
        return accountMapper.toResponse(require(accountId));
    }

    @Override
    @Transactional
    public AccountResponse suspend(UUID accountId, String reason) {
        Account account = require(accountId);
        account.setStatus(AccountStatus.SUSPENDED);
        Account saved = accountRepository.save(account);
        emit("identity.account.suspended", saved,
                reason == null ? Map.of() : Map.of("reason", reason));
        log.info("Suspended account {}", accountId);
        return accountMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AccountResponse activate(UUID accountId) {
        Account account = require(accountId);
        account.setStatus(AccountStatus.ACTIVE);
        Account saved = accountRepository.save(account);
        emit("identity.account.verified", saved, Map.of());
        log.info("Activated account {}", accountId);
        return accountMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AccountResponse setRoles(UUID accountId, List<String> roleNames) {
        Account account = require(accountId);
        Set<Role> resolved = new HashSet<>();
        for (String name : roleNames) {
            Role role = roleRepository.findByName(name)
                    .orElseThrow(() -> ApplicationException.badRequest("Unknown role: " + name));
            resolved.add(role);
        }
        account.setRoles(resolved);
        Account saved = accountRepository.save(account);
        log.info("Set roles {} on account {}", roleNames, accountId);
        return accountMapper.toResponse(saved);
    }

    private Account require(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> ApplicationException.notFound("Account not found"));
    }

    private void emit(String type, Account account, Map<String, Object> data) {
        Set<String> roles = account.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        DomainEvent event = DomainEvent.of(type, account.getId(), roles,
                new DomainEvent.Subject("account", account.getId().toString()), data);
        eventPublisher.publish(DomainEvent.TOPIC, account.getId().toString(), event);
    }
}
