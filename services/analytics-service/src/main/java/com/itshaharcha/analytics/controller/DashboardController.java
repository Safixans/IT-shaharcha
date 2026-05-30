package com.itshaharcha.analytics.controller;

import com.itshaharcha.analytics.dto.response.Dashboard;
import com.itshaharcha.analytics.service.DashboardService;
import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@Tag(name = "dashboards", description = "Aggregated dashboard metrics")
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Aggregated dashboard for an account (cross-domain summaries + series)")
    @GetMapping("/dashboard")
    public ApiResponse<Dashboard> dashboard(
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false, defaultValue = "day") String granularity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Authentication authentication) {
        UUID subject = resolveSubject(accountId, authentication);
        return ApiResponse.ok(dashboardService.dashboard(subject, granularity, from, to));
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
