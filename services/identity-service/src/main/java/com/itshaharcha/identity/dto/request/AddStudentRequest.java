package com.itshaharcha.identity.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddStudentRequest(
        @NotNull UUID studentId) {
}
