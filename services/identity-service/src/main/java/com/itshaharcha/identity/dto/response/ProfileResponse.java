package com.itshaharcha.identity.dto.response;

import com.itshaharcha.identity.dto.request.ProfileLinkDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Mirrors the spec Profile schema. */
public record ProfileResponse(
        UUID accountId,
        String username,
        String fullName,
        String bio,
        String avatarUrl,
        String locale,
        String country,
        List<ProfileLinkDto> links,
        Instant updatedAt) {
}
