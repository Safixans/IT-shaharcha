package com.itshaharcha.portfolio.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.portfolio.entity.Visibility;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** The assembled academic portfolio (spec Portfolio). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PortfolioResponse(
        UUID accountId,
        String handle,
        Visibility visibility,
        Instant publishedAt,
        List<CertificateResponse> certificates,
        List<EducationResponse> education,
        List<PortfolioItemResponse> items) {
}
