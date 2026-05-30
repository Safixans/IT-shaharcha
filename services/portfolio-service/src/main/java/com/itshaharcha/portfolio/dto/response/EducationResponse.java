package com.itshaharcha.portfolio.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.UUID;

/** An education entry (spec Education). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EducationResponse(
        UUID id,
        UUID accountId,
        String institution,
        String degree,
        String fieldOfStudy,
        LocalDate startDate,
        LocalDate endDate,
        String description) {
}
