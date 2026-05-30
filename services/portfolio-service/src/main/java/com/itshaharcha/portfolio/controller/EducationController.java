package com.itshaharcha.portfolio.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.portfolio.dto.request.EducationCreate;
import com.itshaharcha.portfolio.dto.response.EducationResponse;
import com.itshaharcha.portfolio.service.EducationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "education", description = "Education history")
@RestController
@RequestMapping("/api/v1/portfolio/education")
@RequiredArgsConstructor
public class EducationController {

    private final EducationService educationService;

    @Operation(summary = "List the caller's education entries")
    @GetMapping
    public ApiResponse<List<EducationResponse>> list() {
        return ApiResponse.ok(educationService.list());
    }

    @Operation(summary = "Add an education entry")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<EducationResponse> add(@Valid @RequestBody EducationCreate input) {
        return ApiResponse.ok(educationService.add(input));
    }

    @Operation(summary = "Delete an education entry")
    @DeleteMapping("/{educationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID educationId) {
        educationService.delete(educationId);
    }
}
