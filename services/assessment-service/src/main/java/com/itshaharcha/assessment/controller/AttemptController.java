package com.itshaharcha.assessment.controller;

import com.itshaharcha.assessment.dto.request.AttemptSubmit;
import com.itshaharcha.assessment.dto.response.AttemptReport;
import com.itshaharcha.assessment.dto.response.PageResponse;
import com.itshaharcha.assessment.entity.AttemptFamily;
import com.itshaharcha.assessment.service.AttemptService;
import com.itshaharcha.common.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Shared attempt lifecycle. Ownership is enforced in the service (caller's own attempts). */
@RestController
@RequestMapping("/api/v1/assessment/attempts")
@RequiredArgsConstructor
public class AttemptController {

    private final AttemptService attempts;

    @GetMapping
    public ApiResponse<PageResponse<AttemptReport>> myAttempts(
            @RequestParam(required = false) AttemptFamily family,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(attempts.myAttempts(family, PageRequest.of(page, size)));
    }

    @GetMapping("/{attemptId}")
    public ApiResponse<AttemptReport> get(@PathVariable UUID attemptId) {
        return ApiResponse.ok(attempts.get(attemptId));
    }

    @PostMapping("/{attemptId}:autosave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void autosave(@PathVariable UUID attemptId, @RequestBody AttemptSubmit body) {
        attempts.autosave(attemptId, body);
    }

    @PostMapping("/{attemptId}:submit")
    public ApiResponse<AttemptReport> submit(@PathVariable UUID attemptId,
                                             @RequestBody(required = false) AttemptSubmit body) {
        return ApiResponse.ok(attempts.submit(attemptId, body));
    }
}
