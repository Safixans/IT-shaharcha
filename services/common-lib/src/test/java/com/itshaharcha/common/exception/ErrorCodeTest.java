package com.itshaharcha.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodeTest {

    @Test
    void each_code_maps_to_expected_http_status() {
        assertThat(ErrorCode.VALIDATION_FAILED.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ErrorCode.BAD_REQUEST.status()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ErrorCode.UNAUTHORIZED.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ErrorCode.INVALID_CREDENTIALS.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ErrorCode.TOKEN_EXPIRED.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ErrorCode.TOKEN_INVALID.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(ErrorCode.FORBIDDEN.status()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ErrorCode.NOT_FOUND.status()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ErrorCode.CONFLICT.status()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ErrorCode.RATE_LIMITED.status()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(ErrorCode.INTERNAL_ERROR.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void valueOf_roundTrips() {
        for (ErrorCode code : ErrorCode.values()) {
            assertThat(ErrorCode.valueOf(code.name())).isSameAs(code);
        }
    }
}
