package com.itshaharcha.portfolio.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.portfolio.entity.ItemKind;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** A free-form portfolio item (spec PortfolioItem). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PortfolioItemResponse(
        UUID id,
        UUID accountId,
        ItemKind kind,
        String title,
        String description,
        String url,
        UUID fileId,
        List<String> tags,
        Instant createdAt) {
}
