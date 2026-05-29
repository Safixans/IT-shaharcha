package com.itshaharcha.learning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.learning.entity.LessonKind;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LessonResponse(
        UUID id,
        String title,
        int order,
        LessonKind kind,
        Integer estimatedMinutes) {
}
