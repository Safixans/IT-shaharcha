package com.itshaharcha.identity.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.identity.dto.request.SetRolesRequest;
import com.itshaharcha.identity.dto.request.SuspendRequest;
import com.itshaharcha.identity.dto.response.AccountResponse;
import com.itshaharcha.identity.dto.response.PageResponse;
import com.itshaharcha.identity.entity.AccountStatus;
import com.itshaharcha.identity.service.AccountAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Accounts", description = "Administrative account management")
@RestController
@RequestMapping("/api/v1/identity/accounts")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AccountController {

    private final AccountAdminService accountAdminService;

    @Operation(summary = "List accounts (paged, filterable)")
    @GetMapping
    public ApiResponse<PageResponse<AccountResponse>> list(
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(accountAdminService.list(status, q, pageable));
    }

    @Operation(summary = "Get one account by id")
    @GetMapping("/{accountId}")
    public ApiResponse<AccountResponse> get(@PathVariable UUID accountId) {
        return ApiResponse.ok(accountAdminService.get(accountId));
    }

    @Operation(summary = "Suspend an account")
    @PostMapping("/{accountId}:suspend")
    public ApiResponse<AccountResponse> suspend(@PathVariable UUID accountId,
                                                @RequestBody(required = false) SuspendRequest request) {
        String reason = request == null ? null : request.reason();
        return ApiResponse.ok(accountAdminService.suspend(accountId, reason));
    }

    @Operation(summary = "Activate an account")
    @PostMapping("/{accountId}:activate")
    public ApiResponse<AccountResponse> activate(@PathVariable UUID accountId) {
        return ApiResponse.ok(accountAdminService.activate(accountId));
    }

    @Operation(summary = "Replace an account's roles")
    @PutMapping("/{accountId}/roles")
    public ApiResponse<AccountResponse> setRoles(@PathVariable UUID accountId,
                                                 @Valid @RequestBody SetRolesRequest request) {
        return ApiResponse.ok(accountAdminService.setRoles(accountId, request.roles()));
    }
}
