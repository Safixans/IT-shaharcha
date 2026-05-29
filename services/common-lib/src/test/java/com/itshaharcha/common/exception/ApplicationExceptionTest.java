package com.itshaharcha.common.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationExceptionTest {

    @Test
    void notFound_factory_setsCodeAndMessage() {
        ApplicationException ex = ApplicationException.notFound("missing");
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo("missing");
    }

    @Test
    void badRequest_factory_setsCode() {
        assertThat(ApplicationException.badRequest("bad").getErrorCode())
                .isEqualTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    void conflict_factory_setsCode() {
        assertThat(ApplicationException.conflict("dup").getErrorCode())
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    void unauthorized_factory_setsCode() {
        assertThat(ApplicationException.unauthorized("no").getErrorCode())
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    void constructor_withCause_preservesCause() {
        Throwable cause = new IllegalStateException("root");
        ApplicationException ex =
                new ApplicationException(ErrorCode.INTERNAL_ERROR, "boom", cause);
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_ERROR);
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
