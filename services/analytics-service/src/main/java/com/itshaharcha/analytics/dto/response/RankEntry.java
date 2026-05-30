package com.itshaharcha.analytics.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

/** A single leaderboard / ranking entry (spec RankEntry). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RankEntry(
        int rank,
        UUID accountId,
        String username,
        String displayName,
        String avatarUrl,
        String domain,
        long points,
        Integer level,
        Integer delta) {
}
