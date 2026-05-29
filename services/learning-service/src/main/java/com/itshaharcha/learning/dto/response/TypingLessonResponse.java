package com.itshaharcha.learning.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.itshaharcha.learning.entity.Level;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TypingLessonResponse(
        UUID id,
        String title,
        Level difficulty,
        String text) {
}
