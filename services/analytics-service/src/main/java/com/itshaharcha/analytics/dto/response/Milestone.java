package com.itshaharcha.analytics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.UUID;

/** A reached milestone (spec Milestone = MilestoneReachedData + accountId/reachedAt). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Milestone(
        String milestone,
        int points,
        String domain,
        UUID accountId,
        Instant reachedAt) {
}
