package com.itshaharcha.user.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record EducationResponse(
        UUID id,
        String institution,
        String degree,
        String fieldOfStudy,
        LocalDate startDate,
        LocalDate endDate) {
}
