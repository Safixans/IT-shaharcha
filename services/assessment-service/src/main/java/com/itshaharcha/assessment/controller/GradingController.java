package com.itshaharcha.assessment.controller;

import com.itshaharcha.assessment.dto.request.WritingGrade;
import com.itshaharcha.assessment.dto.response.AttemptReport;
import com.itshaharcha.assessment.dto.response.PageResponse;
import com.itshaharcha.assessment.service.GradingService;
import com.itshaharcha.common.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Teacher grading of Writing submissions, scoped to the teacher's own students. */
@RestController
@RequestMapping("/api/v1/assessment/grading")
@RequiredArgsConstructor
public class GradingController {

    private final GradingService grading;

    @GetMapping("/queue")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<PageResponse<AttemptReport>> queue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(grading.queue(PageRequest.of(page, size)));
    }

    @PostMapping("/{attemptId}")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ApiResponse<AttemptReport> grade(@PathVariable UUID attemptId,
                                            @Valid @RequestBody WritingGrade body) {
        return ApiResponse.ok(grading.gradeWriting(attemptId, body));
    }
}
