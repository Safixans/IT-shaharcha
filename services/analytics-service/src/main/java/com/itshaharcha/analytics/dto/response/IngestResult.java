package com.itshaharcha.analytics.dto.response;

import java.util.List;
import java.util.UUID;

/** Outcome of an ingest call (spec IngestResult). */
public record IngestResult(
        int received,
        int accepted,
        int duplicates,
        List<Rejected> rejected) {

    public record Rejected(UUID eventId, String reason) {
    }
}
