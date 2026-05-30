package com.itshaharcha.portfolio.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.UUID;

/** Register a certificate referencing an uploaded fileId (spec CertificateCreate). */
public record CertificateCreate(
        @NotBlank String title,
        String issuer,
        LocalDate issuedOn,
        UUID fileId,
        String credentialUrl) {
}
