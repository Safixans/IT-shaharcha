package com.itshaharcha.analytics.controller;

import com.itshaharcha.analytics.dto.response.Leaderboard;
import com.itshaharcha.analytics.dto.response.RankEntry;
import com.itshaharcha.analytics.security.SecurityUtils;
import com.itshaharcha.analytics.service.RankingService;
import com.itshaharcha.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "rankings", description = "Leaderboards and ranking participation")
@RestController
@RequestMapping("/api/v1/analytics/rankings")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @Operation(summary = "Leaderboard for a domain (or overall)")
    @GetMapping
    public ApiResponse<Leaderboard> leaderboard(
            @RequestParam(required = false) String domain,
            @RequestParam(required = false, defaultValue = "all_time") String period,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return ApiResponse.ok(rankingService.leaderboard(domain, period, pageable));
    }

    @Operation(summary = "The caller's rank across the available boards")
    @GetMapping("/me")
    public ApiResponse<List<RankEntry>> myRank(
            @RequestParam(required = false, defaultValue = "all_time") String period) {
        return ApiResponse.ok(rankingService.myRank(SecurityUtils.currentAccountId(), period));
    }
}
