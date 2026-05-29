package com.itshaharcha.learning.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Body for POST tutorials/{id}:watched. */
public record TutorialWatchedInput(
        @NotNull @PositiveOrZero Integer watchedSeconds,
        @PositiveOrZero Integer positionSeconds,
        Boolean completed) {
}
