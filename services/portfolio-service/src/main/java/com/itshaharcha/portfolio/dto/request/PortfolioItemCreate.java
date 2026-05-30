package com.itshaharcha.portfolio.dto.request;

import com.itshaharcha.portfolio.entity.ItemKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/** Add a portfolio item (spec PortfolioItemCreate). */
public record PortfolioItemCreate(
        @NotNull ItemKind kind,
        @NotBlank String title,
        @Size(max = 2000) String description,
        String url,
        UUID fileId,
        List<String> tags) {
}
