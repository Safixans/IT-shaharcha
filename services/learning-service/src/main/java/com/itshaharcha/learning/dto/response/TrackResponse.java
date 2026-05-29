package com.itshaharcha.learning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TrackResponse(
        UUID id,
        String title,
        String slug,
        String description,
        int courseCount) {
}
