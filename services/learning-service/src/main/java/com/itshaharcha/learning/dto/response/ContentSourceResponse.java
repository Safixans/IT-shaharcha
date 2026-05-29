package com.itshaharcha.learning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.learning.entity.SourceStatus;
import com.itshaharcha.learning.entity.SourceTarget;
import com.itshaharcha.learning.entity.SourceType;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ContentSourceResponse(
        UUID id,
        String name,
        SourceType type,
        SourceTarget target,
        String url,
        boolean enabled,
        String schedule,
        String defaultTopic,
        SourceStatus status,
        Instant createdAt,
        Instant lastSyncedAt,
        String lastError,
        int itemCount) {
}
