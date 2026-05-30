package com.itshaharcha.assessment.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.assessment.entity.ExamType;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExamDetailResponse(
        UUID id,
        String title,
        ExamType examType,
        String description,
        Integer durationMinutes,
        int sectionCount,
        boolean isRealExam,
        List<SectionResponse> sections) {
}
