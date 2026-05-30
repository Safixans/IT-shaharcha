package com.itshaharcha.analytics.controller;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import com.itshaharcha.analytics.dto.response.IngestResult;
import com.itshaharcha.analytics.event.DomainEvent;
import com.itshaharcha.analytics.service.IngestService;
import com.itshaharcha.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * REST fallback for the Kafka event bus (spec ingestEvents). Accepts a single DomainEvent
 * or an array; idempotent on eventId. Restricted to admins — services normally publish to
 * Kafka, so REST ingest is for backfills/imports/broker-less environments.
 */
@Tag(name = "ingest", description = "Event ingestion (Kafka fallback over REST)")
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class IngestController {

    private final IngestService ingestService;
    private final JsonMapper objectMapper;

    @Operation(summary = "Ingest one or more DomainEvents (REST fallback for Kafka)")
    @PostMapping("/events:ingest")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<IngestResult> ingest(@RequestBody JsonNode body) {
        return ApiResponse.ok(ingestService.ingest(parse(body)));
    }

    private List<DomainEvent> parse(JsonNode body) {
        List<DomainEvent> events = new ArrayList<>();
        if (body == null || body.isNull() || body.isMissingNode()) {
            return events;
        }
        if (body.isArray()) {
            for (JsonNode node : body) {
                events.add(objectMapper.treeToValue(node, DomainEvent.class));
            }
        } else {
            events.add(objectMapper.treeToValue(body, DomainEvent.class));
        }
        return events;
    }
}
