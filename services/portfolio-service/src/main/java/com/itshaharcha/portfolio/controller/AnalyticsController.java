package com.itshaharcha.portfolio.controller;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.portfolio.dto.response.DomainAnalyticsSummary;
import com.itshaharcha.portfolio.dto.response.MetricSeriesSet;
import com.itshaharcha.portfolio.dto.response.PageResponse;
import com.itshaharcha.portfolio.event.DomainEvent;
import com.itshaharcha.portfolio.service.AnalyticsService;
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

@Tag(name = "analytics", description = "Uniform domain analytics read API")
@RestController
@RequestMapping("/api/v1/portfolio/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Domain analytics summary for an account")
    @GetMapping("/summary")
    public ApiResponse<DomainAnalyticsSummary> summary(
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Authentication authentication) {
        UUID subject = resolveSubject(accountId, authentication);
        return ApiResponse.ok(analyticsService.summary(subject, from, to));
    }

    @Operation(summary = "Recent domain activity feed for an account")
    @GetMapping("/activity")
    public ApiResponse<PageResponse<DomainEvent>> activity(
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {
        UUID subject = resolveSubject(accountId, authentication);
        return ApiResponse.ok(analyticsService.activity(subject, from, to, pageable));
    }

    @Operation(summary = "Time-bucketed metric series for an account")
    @GetMapping("/metrics")
    public ApiResponse<MetricSeriesSet> metrics(
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) String metric,
            @RequestParam(required = false, defaultValue = "day") String granularity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Authentication authentication) {
        UUID subject = resolveSubject(accountId, authentication);
        return ApiResponse.ok(analyticsService.metrics(subject, metric, granularity, from, to));
    }

    /** Default to the authenticated actor; reporting on a different account requires ROLE_ADMIN. */
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
