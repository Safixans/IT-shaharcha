package com.itshaharcha.assessment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SectionResponse(
        UUID id,
        String name,
        int order,
        int questionCount,
        Integer durationMinutes,
        String content,
        String pdfUrl) {
}
