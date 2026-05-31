package com.itshaharcha.learning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RoadmapNodeResponse(
        String nodeKey,
        String type,
        String title,
        String summary,
        String detail,
        boolean optional,
        int orderIndex,
        Double posX,
        Double posY,
        UUID courseId,
        String courseTitle) {
}
