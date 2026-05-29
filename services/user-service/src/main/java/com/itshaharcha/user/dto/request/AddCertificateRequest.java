package com.itshaharcha.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record AddCertificateRequest(

        @NotBlank @Size(max = 200)
        String title,

        @Size(max = 200)
        String issuer,

        UUID fileId,

        @Size(max = 512)
        String credentialUrl,

        LocalDate issuedAt) {
}
