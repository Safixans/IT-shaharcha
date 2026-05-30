package com.itshaharcha.assessment.controller;

import com.itshaharcha.assessment.dto.response.ExamDetailResponse;
import com.itshaharcha.assessment.dto.response.ExamResponse;
import com.itshaharcha.assessment.dto.response.PageResponse;
import com.itshaharcha.assessment.entity.ExamType;
import com.itshaharcha.assessment.service.CatalogService;
import com.itshaharcha.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "catalog", description = "Public exam catalog")
@RestController
@RequestMapping("/api/v1/assessment")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @Operation(summary = "List available exams")
    @GetMapping("/exams")
    public ApiResponse<PageResponse<ExamResponse>> listExams(
            @RequestParam(required = false) ExamType examType,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(catalogService.listExams(examType, pageable));
    }

    @Operation(summary = "Get an exam with its section structure")
    @GetMapping("/exams/{examId}")
    public ApiResponse<ExamDetailResponse> getExam(@PathVariable UUID examId) {
        return ApiResponse.ok(catalogService.getExam(examId));
    }
}
