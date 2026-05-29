package com.itshaharcha.learning.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.learning.dto.response.CourseDetailResponse;
import com.itshaharcha.learning.dto.response.CourseResponse;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.dto.response.TrackResponse;
import com.itshaharcha.learning.entity.Level;
import com.itshaharcha.learning.service.CatalogService;
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

@Tag(name = "catalog", description = "Public learning catalog")
@RestController
@RequestMapping("/api/v1/learning")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @Operation(summary = "List skill tracks")
    @GetMapping("/tracks")
    public ApiResponse<PageResponse<TrackResponse>> listTracks(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(catalogService.listTracks(q, pageable));
    }

    @Operation(summary = "List courses, optionally filtered by track")
    @GetMapping("/courses")
    public ApiResponse<PageResponse<CourseResponse>> listCourses(
            @RequestParam(required = false) UUID trackId,
            @RequestParam(required = false) Level level,
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(catalogService.listCourses(trackId, level, pageable));
    }

    @Operation(summary = "Get a course with its modules and lessons")
    @GetMapping("/courses/{courseId}")
    public ApiResponse<CourseDetailResponse> getCourse(@PathVariable UUID courseId) {
        return ApiResponse.ok(catalogService.getCourse(courseId));
    }
}
