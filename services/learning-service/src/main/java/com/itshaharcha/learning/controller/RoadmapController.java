package com.itshaharcha.learning.controller;

import com.itshaharcha.common.web.ApiResponse;
import com.itshaharcha.learning.dto.response.PageResponse;
import com.itshaharcha.learning.dto.response.RoadmapCardResponse;
import com.itshaharcha.learning.dto.response.RoadmapDetailResponse;
import com.itshaharcha.learning.service.RoadmapService;
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

@Tag(name = "roadmaps", description = "Public roadmap catalog and graph")
@RestController
@RequestMapping("/api/v1/learning/roadmaps")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;

    @Operation(summary = "List published roadmaps")
    @GetMapping
    public ApiResponse<PageResponse<RoadmapCardResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String kind,
            @PageableDefault(size = 50) Pageable pageable) {
        return ApiResponse.ok(roadmapService.listRoadmaps(q, kind, pageable));
    }

    @Operation(summary = "Get a roadmap with its full node/edge graph")
    @GetMapping("/{slug}")
    public ApiResponse<RoadmapDetailResponse> get(@PathVariable String slug) {
        return ApiResponse.ok(roadmapService.getRoadmap(slug));
    }
}
