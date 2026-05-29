package com.itshaharcha.user.dto.response;

import java.util.List;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        UUID accountId,
        String fullName,
        String avatarUrl,
        String bio,
        String country,
        List<EducationResponse> educationHistory,
        List<CertificateResponse> certificates,
        List<PortfolioItemResponse> portfolioItems) {
}
