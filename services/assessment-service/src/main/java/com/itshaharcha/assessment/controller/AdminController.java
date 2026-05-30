package com.itshaharcha.assessment.controller;

import com.itshaharcha.assessment.dto.request.ExamInput;
import com.itshaharcha.assessment.dto.request.QuestionInput;
import com.itshaharcha.assessment.dto.request.SectionInput;
import com.itshaharcha.assessment.dto.response.ExamResponse;
import com.itshaharcha.assessment.dto.response.QuestionResponse;
import com.itshaharcha.assessment.dto.response.SectionResponse;
import com.itshaharcha.assessment.service.AdminService;
import com.itshaharcha.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "admin", description = "Exam authoring: exams, sections, questions")
@RestController
@RequestMapping("/api/v1/assessment/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "Create an exam")
    @PostMapping("/exams")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('EXAM_WRITE')")
    public ApiResponse<ExamResponse> createExam(@Valid @RequestBody ExamInput input) {
        return ApiResponse.ok(adminService.createExam(input));
    }

    @Operation(summary = "Update an exam")
    @PatchMapping("/exams/{examId}")
    @PreAuthorize("hasAuthority('EXAM_EDIT')")
    public ApiResponse<ExamResponse> updateExam(@PathVariable UUID examId,
                                                @Valid @RequestBody ExamInput input) {
        return ApiResponse.ok(adminService.updateExam(examId, input));
    }

    @Operation(summary = "Delete an exam")
    @DeleteMapping("/exams/{examId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('EXAM_DELETE')")
    public void deleteExam(@PathVariable UUID examId) {
        adminService.deleteExam(examId);
    }

    @Operation(summary = "Add a section to an exam")
    @PostMapping("/exams/{examId}/sections")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SECTION_WRITE')")
    public ApiResponse<SectionResponse> createSection(@PathVariable UUID examId,
                                                      @Valid @RequestBody SectionInput input) {
        return ApiResponse.ok(adminService.createSection(examId, input));
    }

    @Operation(summary = "Update a section")
    @PatchMapping("/sections/{sectionId}")
    @PreAuthorize("hasAuthority('SECTION_EDIT')")
    public ApiResponse<SectionResponse> updateSection(@PathVariable UUID sectionId,
                                                      @Valid @RequestBody SectionInput input) {
        return ApiResponse.ok(adminService.updateSection(sectionId, input));
    }

    @Operation(summary = "Delete a section")
    @DeleteMapping("/sections/{sectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('SECTION_DELETE')")
    public void deleteSection(@PathVariable UUID sectionId) {
        adminService.deleteSection(sectionId);
    }

    @Operation(summary = "Add a question to a section")
    @PostMapping("/sections/{sectionId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('QUESTION_WRITE')")
    public ApiResponse<QuestionResponse> createQuestion(@PathVariable UUID sectionId,
                                                        @Valid @RequestBody QuestionInput input) {
        return ApiResponse.ok(adminService.createQuestion(sectionId, input));
    }

    @Operation(summary = "Update a question")
    @PatchMapping("/questions/{questionId}")
    @PreAuthorize("hasAuthority('QUESTION_EDIT')")
    public ApiResponse<QuestionResponse> updateQuestion(@PathVariable UUID questionId,
                                                        @Valid @RequestBody QuestionInput input) {
        return ApiResponse.ok(adminService.updateQuestion(questionId, input));
    }

    @Operation(summary = "Delete a question")
    @DeleteMapping("/questions/{questionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('QUESTION_DELETE')")
    public void deleteQuestion(@PathVariable UUID questionId) {
        adminService.deleteQuestion(questionId);
    }
}
