package com.itshaharcha.portfolio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Add an education entry (spec EducationCreate). */
public record EducationCreate(
        @NotBlank String institution,
        String degree,
        String fieldOfStudy,
        LocalDate startDate,
        LocalDate endDate,
        @Size(max = 1000) String description) {
}
