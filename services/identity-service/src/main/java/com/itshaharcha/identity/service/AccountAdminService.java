package com.itshaharcha.identity.service;

import com.itshaharcha.identity.dto.response.AccountResponse;
import com.itshaharcha.identity.dto.response.PageResponse;
import com.itshaharcha.identity.entity.AccountStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AccountAdminService {

    PageResponse<AccountResponse> list(AccountStatus status, String q, Pageable pageable);

    AccountResponse get(UUID accountId);

    AccountResponse suspend(UUID accountId, String reason);

    AccountResponse activate(UUID accountId);

    AccountResponse setRoles(UUID accountId, List<String> roleNames);
}
