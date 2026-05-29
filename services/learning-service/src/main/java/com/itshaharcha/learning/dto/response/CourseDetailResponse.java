package com.itshaharcha.learning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.learning.entity.Level;

import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CourseDetailResponse(
        UUID id,
        UUID trackId,
        String title,
        String slug,
        String summary,
        Level level,
        int lessonCount,
        Integer estimatedMinutes,
        List<ModuleResponse> modules) {
}
