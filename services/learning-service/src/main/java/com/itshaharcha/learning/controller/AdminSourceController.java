package com.itshaharcha.learning.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.learning.dto.request.ContentSourceInput;
import com.itshaharcha.learning.dto.response.ContentSourceResponse;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.dto.response.SourceSyncRunResponse;
import com.itshaharcha.learning.entity.SourceType;
import com.itshaharcha.learning.service.ContentSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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

import java.util.UUID;

@Tag(name = "admin-sources", description = "Authoring: external content sources (feeds)")
@RestController
@RequestMapping("/api/v1/learning/admin/sources")
@RequiredArgsConstructor
public class AdminSourceController {

    private final ContentSourceService contentSourceService;

    @Operation(summary = "List registered content sources")
    @GetMapping
    @PreAuthorize("hasAuthority('SOURCE_READ')")
    public ApiResponse<PageResponse<ContentSourceResponse>> listSources(
            @RequestParam(required = false) SourceType type,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(contentSourceService.listSources(type, pageable));
    }

    @Operation(summary = "Register a content source")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SOURCE_WRITE')")
    public ApiResponse<ContentSourceResponse> createSource(
            @Valid @RequestBody ContentSourceInput input) {
        return ApiResponse.ok(contentSourceService.createSource(input));
    }

    @Operation(summary = "Get a content source")
    @GetMapping("/{sourceId}")
    @PreAuthorize("hasAuthority('SOURCE_READ')")
    public ApiResponse<ContentSourceResponse> getSource(@PathVariable UUID sourceId) {
        return ApiResponse.ok(contentSourceService.getSource(sourceId));
    }

    @Operation(summary = "Update a content source")
    @PatchMapping("/{sourceId}")
    @PreAuthorize("hasAuthority('SOURCE_EDIT')")
    public ApiResponse<ContentSourceResponse> updateSource(
            @PathVariable UUID sourceId, @Valid @RequestBody ContentSourceInput input) {
        return ApiResponse.ok(contentSourceService.updateSource(sourceId, input));
    }

    @Operation(summary = "Delete a content source")
    @DeleteMapping("/{sourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('SOURCE_DELETE')")
    public void deleteSource(@PathVariable UUID sourceId) {
        contentSourceService.deleteSource(sourceId);
    }

    @Operation(summary = "Trigger an immediate sync of a content source")
    @PostMapping("/{sourceId}:sync")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAuthority('SOURCE_WRITE')")
    public ApiResponse<SourceSyncRunResponse> syncSource(@PathVariable UUID sourceId) {
        return ApiResponse.ok(contentSourceService.sync(sourceId));
    }
}
