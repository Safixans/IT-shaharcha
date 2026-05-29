package com.itshaharcha.common.exception;

import com.itshaharcha.common.web.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private MockHttpServletRequest request(String uri) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI(uri);
        return req;
    }

    @Test
    void handleApplication_mapsClientErrorToStatusAndCode() {
        ApplicationException ex = ApplicationException.notFound("nope");

        ResponseEntity<ErrorResponse> response =
                handler.handleApplication(ex, request("/api/v1/x"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.code()).isEqualTo("NOT_FOUND");
        assertThat(body.message()).isEqualTo("nope");
        assertThat(body.path()).isEqualTo("/api/v1/x");
        assertThat(body.status()).isEqualTo(404);
    }

    @Test
    void handleApplication_logsAndMapsServerError() {
        ApplicationException ex =
                new ApplicationException(ErrorCode.INTERNAL_ERROR, "kaboom");

        ResponseEntity<ErrorResponse> response =
                handler.handleApplication(ex, request("/api/v1/y"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
    }

    @Test
    void handleValidation_collectsFieldViolations() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult binding = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(binding);
        when(binding.getFieldErrors()).thenReturn(List.of(
                new FieldError("obj", "email", "must not be blank"),
                new FieldError("obj", "password", "too short")));

        ResponseEntity<ErrorResponse> response =
                handler.handleValidation(ex, request("/api/v1/auth/register"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body.code()).isEqualTo("VALIDATION_FAILED");
        assertThat(body.errors()).hasSize(2);
        assertThat(body.errors().get(0).field()).isEqualTo("email");
        assertThat(body.errors().get(0).message()).isEqualTo("must not be blank");
    }

    @Test
    void handleUnexpected_returns500WithGenericMessage() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUnexpected(new RuntimeException("leak"), request("/boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse body = response.getBody();
        assertThat(body.code()).isEqualTo("INTERNAL_ERROR");
        assertThat(body.message()).isEqualTo("An unexpected error occurred");
        assertThat(body.message()).doesNotContain("leak");
    }
}
