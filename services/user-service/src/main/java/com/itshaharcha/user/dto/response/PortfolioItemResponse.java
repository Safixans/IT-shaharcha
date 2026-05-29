package com.itshaharcha.user.dto.response;

import com.itshaharcha.user.entity.PortfolioItemType;

import java.util.UUID;

public record PortfolioItemResponse(
        UUID id,
        PortfolioItemType type,
        String title,
        String description,
        String url) {
}
