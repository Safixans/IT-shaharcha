package com.itshaharcha.portfolio.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.portfolio.entity.VerificationStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** A certificate (spec Certificate). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CertificateResponse(
        UUID id,
        UUID accountId,
        String title,
        String issuer,
        LocalDate issuedOn,
        UUID fileId,
        String credentialUrl,
        VerificationStatus status,
        Instant createdAt,
        Instant verifiedAt) {
}
