package com.itshaharcha.learning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.learning.entity.SyncStatus;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SourceSyncRunResponse(
        UUID sourceId,
        UUID runId,
        SyncStatus status,
        Instant startedAt,
        Integer itemsImported) {
}
