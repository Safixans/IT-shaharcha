package com.itshaharcha.learning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TutorialResponse(
        UUID id,
        String title,
        String topic,
        String videoUrl,
        Integer durationSeconds,
        String thumbnailUrl) {
}
