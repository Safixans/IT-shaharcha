package com.itshaharcha.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AddEducationRequest(

        @NotBlank @Size(max = 200)
        String institution,

        @Size(max = 120)
        String degree,

        @Size(max = 120)
        String fieldOfStudy,

        LocalDate startDate,

        LocalDate endDate) {
}
