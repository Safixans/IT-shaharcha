package com.itshaharcha.assessment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.assessment.entity.ExamType;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExamResponse(
        UUID id,
        String title,
        ExamType examType,
        String description,
        Integer durationMinutes,
        int sectionCount,
        boolean isRealExam) {
}
