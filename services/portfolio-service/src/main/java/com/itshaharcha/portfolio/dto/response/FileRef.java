package com.itshaharcha.portfolio.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

/** Stored file reference returned after upload (spec FileRef). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FileRef(
        UUID fileId,
        String contentType,
        long sizeBytes,
        String url) {
}
