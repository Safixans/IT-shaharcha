package com.itshaharcha.identity.controller;

import com.itshaharcha.common.exception.ApplicationException;
import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.identity.dto.response.MemberResponse;
import com.itshaharcha.identity.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * The caller's (teacher's) own students. {@code /me/students/{id}} is the grading-authorization
 * check other services call with the teacher's forwarded token (200 = mine, 404 = not mine).
 */
@Tag(name = "My students", description = "A teacher's students across their groups")
@RestController
@RequestMapping("/api/v1/identity/me/students")
@RequiredArgsConstructor
public class TeacherStudentsController {

    private final GroupService groupService;

    @Operation(summary = "List my students")
    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<List<MemberResponse>> myStudents(Authentication auth) {
        return ApiResponse.ok(groupService.studentsOf((UUID) auth.getPrincipal()));
    }

    @Operation(summary = "Check whether a student is mine (grading authorization)")
    @GetMapping("/{studentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<MemberResponse> myStudent(@PathVariable UUID studentId, Authentication auth) {
        UUID teacherId = (UUID) auth.getPrincipal();
        return groupService.studentsOf(teacherId).stream()
                .filter(m -> m.studentId().equals(studentId))
                .findFirst()
                .map(ApiResponse::ok)
                .orElseThrow(() -> ApplicationException.notFound("Not your student"));
    }
}
