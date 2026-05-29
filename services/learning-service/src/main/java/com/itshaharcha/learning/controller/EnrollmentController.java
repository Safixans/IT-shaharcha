package com.itshaharcha.learning.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.learning.dto.request.LessonCompletedInput;
import com.itshaharcha.learning.dto.response.EnrollmentResponse;
import com.itshaharcha.learning.dto.response.LessonProgressResponse;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "enrollment", description = "Enrollment and lesson progress (any authenticated learner)")
@RestController
@RequestMapping("/api/v1/learning")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Operation(summary = "Enroll the caller in a course")
    @PostMapping("/courses/{courseId}:enroll")
    public ApiResponse<EnrollmentResponse> enroll(@PathVariable UUID courseId) {
        return ApiResponse.ok(enrollmentService.enroll(courseId));
    }

    @Operation(summary = "List the caller's enrollments with progress")
    @GetMapping("/enrollments")
    public ApiResponse<PageResponse<EnrollmentResponse>> listMyEnrollments(
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(enrollmentService.listMyEnrollments(pageable));
    }

    @Operation(summary = "Mark a lesson started")
    @PostMapping("/lessons/{lessonId}:start")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void startLesson(@PathVariable UUID lessonId) {
        enrollmentService.startLesson(lessonId);
    }

    @Operation(summary = "Mark a lesson completed")
    @PostMapping("/lessons/{lessonId}:complete")
    public ApiResponse<LessonProgressResponse> completeLesson(
            @PathVariable UUID lessonId,
            @Valid @RequestBody LessonCompletedInput input) {
        return ApiResponse.ok(enrollmentService.completeLesson(lessonId, input));
    }
}
