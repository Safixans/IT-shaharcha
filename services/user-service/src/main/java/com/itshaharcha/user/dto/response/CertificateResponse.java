package com.itshaharcha.user.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record CertificateResponse(
        UUID id,
        String title,
        String issuer,
        UUID fileId,
        String credentialUrl,
        LocalDate issuedAt) {
}
