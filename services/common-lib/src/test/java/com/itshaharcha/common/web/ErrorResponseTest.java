package com.itshaharcha.common.web;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void of_withoutErrors_leavesErrorsNull() {
        ErrorResponse r = ErrorResponse.of(404, "NOT_FOUND", "missing", "/x");
        assertThat(r.status()).isEqualTo(404);
        assertThat(r.code()).isEqualTo("NOT_FOUND");
        assertThat(r.message()).isEqualTo("missing");
        assertThat(r.path()).isEqualTo("/x");
        assertThat(r.errors()).isNull();
        assertThat(r.timestamp()).isNotNull();
    }

    @Test
    void of_withErrors_retainsViolations() {
        var violations = List.of(new ErrorResponse.FieldViolation("email", "blank"));
        ErrorResponse r = ErrorResponse.of(400, "VALIDATION_FAILED", "bad", "/y", violations);
        assertThat(r.errors()).hasSize(1);
        assertThat(r.errors().get(0).field()).isEqualTo("email");
        assertThat(r.errors().get(0).message()).isEqualTo("blank");
    }
}
