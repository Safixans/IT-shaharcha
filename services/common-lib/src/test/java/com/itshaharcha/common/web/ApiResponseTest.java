package com.itshaharcha.common.web;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void ok_wrapsDataWithNoMessage() {
        ApiResponse<String> r = ApiResponse.ok("payload");
        assertThat(r.success()).isTrue();
        assertThat(r.data()).isEqualTo("payload");
        assertThat(r.message()).isNull();
        assertThat(r.timestamp()).isNotNull();
    }

    @Test
    void ok_withMessage_setsBoth() {
        ApiResponse<Integer> r = ApiResponse.ok(42, "done");
        assertThat(r.success()).isTrue();
        assertThat(r.data()).isEqualTo(42);
        assertThat(r.message()).isEqualTo("done");
    }

    @Test
    void message_hasNullDataAndSuccessTrue() {
        ApiResponse<Void> r = ApiResponse.message("hi");
        assertThat(r.success()).isTrue();
        assertThat(r.data()).isNull();
        assertThat(r.message()).isEqualTo("hi");
        assertThat(r.timestamp()).isNotNull();
    }
}
