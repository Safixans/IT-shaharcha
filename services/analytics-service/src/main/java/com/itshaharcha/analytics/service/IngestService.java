package com.itshaharcha.analytics.service;

import com.itshaharcha.analytics.dto.response.IngestResult;
import com.itshaharcha.analytics.event.DomainEvent;

import java.util.List;

public interface IngestService {

    /** Fold a batch of events; returns per-batch accepted/duplicate/rejected counts. */
    IngestResult ingest(List<DomainEvent> events);

    /** Fold a single event (used by the Kafka consumer). Idempotent on eventId. */
    Outcome ingestOne(DomainEvent event);

    enum Status {ACCEPTED, DUPLICATE, REJECTED}

    /** Result of folding one event; carries a reason when rejected. */
    record Outcome(Status status, String reason) {

        public static Outcome accepted() {
            return new Outcome(Status.ACCEPTED, null);
        }

        public static Outcome duplicate() {
            return new Outcome(Status.DUPLICATE, null);
        }

        public static Outcome rejected(String reason) {
            return new Outcome(Status.REJECTED, reason);
        }
    }
}
