package com.itshaharcha.learning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RoadmapDetailResponse(
        UUID id,
        String slug,
        String title,
        String tagline,
        String description,
        String icon,
        String kind,
        String difficulty,
        String layoutMode,
        List<RoadmapNodeResponse> nodes,
        List<RoadmapEdgeResponse> edges) {
}
