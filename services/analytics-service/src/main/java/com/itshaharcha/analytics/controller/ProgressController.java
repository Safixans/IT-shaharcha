package com.itshaharcha.analytics.controller;

import com.itshaharcha.analytics.dto.response.Milestone;
import com.itshaharcha.analytics.dto.response.PageResponse;
import com.itshaharcha.analytics.dto.response.ProgressOverview;
import com.itshaharcha.analytics.service.ProgressService;
import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@Tag(name = "progress", description = "Cross-domain personal progress")
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @Operation(summary = "Cross-domain progress for an account (all domains rolled up)")
    @GetMapping("/progress")
    public ApiResponse<ProgressOverview> progress(
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Authentication authentication) {
        UUID subject = resolveSubject(accountId, authentication);
        return ApiResponse.ok(progressService.progress(subject, from, to));
    }

    @Operation(summary = "Milestones reached by an account")
    @GetMapping("/milestones")
    public ApiResponse<PageResponse<Milestone>> milestones(
            @RequestParam(required = false) UUID accountId,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        UUID subject = resolveSubject(accountId, authentication);
        return ApiResponse.ok(progressService.milestones(subject, pageable));
    }

    private UUID resolveSubject(UUID requested, Authentication authentication) {
        UUID self = (UUID) authentication.getPrincipal();
        if (requested == null || requested.equals(self)) {
            return self;
        }
        boolean admin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (!admin) {
            throw ApplicationException.forbidden("Cannot read analytics for another account");
        }
        return requested;
    }
}
