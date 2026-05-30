package com.itshaharcha.analytics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/** A page of ranked leaderboard entries (spec Leaderboard). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Leaderboard(
        String domain,
        String period,
        Instant generatedAt,
        List<RankEntry> entries,
        PageMeta meta) {
}
