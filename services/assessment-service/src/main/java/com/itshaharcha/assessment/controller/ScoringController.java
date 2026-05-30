package com.itshaharcha.assessment.controller;

import com.itshaharcha.assessment.dto.response.ExamResultResponse;
import com.itshaharcha.assessment.dto.response.PageResponse;
import com.itshaharcha.assessment.entity.ExamType;
import com.itshaharcha.assessment.service.ScoringService;
import com.itshaharcha.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@Tag(name = "scoring", description = "Scores and results")
@RestController
@RequestMapping("/api/v1/assessment")
@RequiredArgsConstructor
public class ScoringController {

    private final ScoringService scoringService;

    @Operation(summary = "Get the scored result for a session")
    @GetMapping("/sessions/{sessionId}/result")
    public ApiResponse<ExamResultResponse> getResult(@PathVariable UUID sessionId) {
        return ApiResponse.ok(scoringService.getResult(sessionId));
    }

    @Operation(summary = "List the caller's exam results")
    @GetMapping("/results")
    public ApiResponse<PageResponse<ExamResultResponse>> listMyResults(
            @RequestParam(required = false) ExamType examType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(scoringService.listMyResults(examType, from, to, pageable));
    }
}
