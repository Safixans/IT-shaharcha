package com.itshaharcha.learning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RoadmapCardResponse(
        UUID id,
        String slug,
        String title,
        String tagline,
        String icon,
        String kind,
        String difficulty,
        int nodeCount) {
}
