package com.itshaharcha.learning.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;

/** Body for POST docs/{id}:read (all fields optional). */
public record DocReadInput(
        @PositiveOrZero Integer durationSeconds,
        @DecimalMin("0") @DecimalMax("100") Double scrollPercent) {
}
