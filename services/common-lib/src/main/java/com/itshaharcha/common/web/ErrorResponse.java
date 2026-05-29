package com.itshaharcha.common.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/** Consistent error body returned across all services. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        List<FieldViolation> errors) {

    public record FieldViolation(String field, String message) {
    }

    public static ErrorResponse of(int status, String code, String message, String path) {
        return new ErrorResponse(Instant.now(), status, code, message, path, null);
    }

    public static ErrorResponse of(int status, String code, String message, String path,
                                   List<FieldViolation> errors) {
        return new ErrorResponse(Instant.now(), status, code, message, path, errors);
    }
}
