package com.itshaharcha.identity.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.identity.dto.request.RoleInput;
import com.itshaharcha.identity.dto.response.RoleResponse;
import com.itshaharcha.identity.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Roles", description = "Role catalog administration")
@RestController
@RequestMapping("/api/v1/identity/roles")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "List all roles")
    @GetMapping
    public ApiResponse<List<RoleResponse>> list() {
        return ApiResponse.ok(roleService.list());
    }

    @Operation(summary = "Create a role")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoleResponse> create(@Valid @RequestBody RoleInput request) {
        return ApiResponse.ok(roleService.create(request));
    }

    @Operation(summary = "Delete a role by name")
    @DeleteMapping("/{roleName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String roleName) {
        roleService.delete(roleName);
    }
}
