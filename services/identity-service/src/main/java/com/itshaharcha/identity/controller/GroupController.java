package com.itshaharcha.identity.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.identity.dto.request.AddStudentRequest;
import com.itshaharcha.identity.dto.request.GroupInput;
import com.itshaharcha.identity.dto.request.GroupUpdate;
import com.itshaharcha.identity.dto.response.GroupResponse;
import com.itshaharcha.identity.dto.response.MemberResponse;
import com.itshaharcha.identity.dto.response.PageResponse;
import com.itshaharcha.identity.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Groups", description = "Teacher groups & student membership")
@RestController
@RequestMapping("/api/v1/identity/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @Operation(summary = "Create a group (admin/moderator)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ApiResponse<GroupResponse> create(@Valid @RequestBody GroupInput input) {
        return ApiResponse.ok(groupService.create(input));
    }

    @Operation(summary = "List groups (admin/moderator), optionally by teacher")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ApiResponse<PageResponse<GroupResponse>> list(
            @RequestParam(required = false) UUID teacherId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(groupService.list(teacherId, pageable));
    }

    @Operation(summary = "Get a group (admin/moderator or the group's teacher)")
    @GetMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','TEACHER')")
    public ApiResponse<GroupResponse> get(@PathVariable UUID groupId, Authentication auth) {
        return ApiResponse.ok(groupService.get(groupId, callerId(auth), privileged(auth)));
    }

    @Operation(summary = "Update a group (admin/moderator)")
    @PatchMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ApiResponse<GroupResponse> update(@PathVariable UUID groupId,
                                             @RequestBody GroupUpdate input) {
        return ApiResponse.ok(groupService.update(groupId, input));
    }

    @Operation(summary = "Delete a group (admin/moderator)")
    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public void delete(@PathVariable UUID groupId) {
        groupService.delete(groupId);
    }

    @Operation(summary = "List a group's students (admin/moderator or the group's teacher)")
    @GetMapping("/{groupId}/students")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','TEACHER')")
    public ApiResponse<List<MemberResponse>> members(@PathVariable UUID groupId, Authentication auth) {
        return ApiResponse.ok(groupService.listMembers(groupId, callerId(auth), privileged(auth)));
    }

    @Operation(summary = "Add a student to a group (admin/moderator or the group's teacher)")
    @PostMapping("/{groupId}/students")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','TEACHER')")
    public ApiResponse<MemberResponse> addStudent(@PathVariable UUID groupId,
                                                  @Valid @RequestBody AddStudentRequest request,
                                                  Authentication auth) {
        return ApiResponse.ok(
                groupService.addStudent(groupId, request.studentId(), callerId(auth), privileged(auth)));
    }

    @Operation(summary = "Remove a student from a group (admin/moderator or the group's teacher)")
    @DeleteMapping("/{groupId}/students/{studentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR','TEACHER')")
    public void removeStudent(@PathVariable UUID groupId, @PathVariable UUID studentId,
                              Authentication auth) {
        groupService.removeStudent(groupId, studentId, callerId(auth), privileged(auth));
    }

    private UUID callerId(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }

    private boolean privileged(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_MODERATOR"));
    }
}
