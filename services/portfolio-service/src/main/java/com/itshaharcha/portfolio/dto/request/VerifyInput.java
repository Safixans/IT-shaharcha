package com.itshaharcha.portfolio.dto.request;

/** Reviewer decision on a certificate (spec verifyCertificate body). Both fields optional. */
public record VerifyInput(
        Boolean verified,
        String note) {
}
