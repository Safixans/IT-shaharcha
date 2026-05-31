package com.itshaharcha.learning.dto.response;

public record RoadmapEdgeResponse(
        String fromNodeKey,
        String toNodeKey,
        String kind,
        String style) {
}
