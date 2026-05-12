package com.clientjourney.app;

import com.clientjourney.app.dto.ApiErrorResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void should_build_bad_request_payload() {
        var response = handler.onIllegalArgument(HttpRequest.GET("/api/admin/scenarios"), new IllegalArgumentException("bad input"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());

        ApiErrorResponse body = response.body();
        assertNotNull(body);
        assertEquals("VALIDATION_ERROR", body.code());
        assertEquals("bad input", body.message());
        assertEquals("/api/admin/scenarios", body.path());
        assertNotNull(body.timestamp());
    }

    @Test
    void should_build_internal_error_payload() {
        var response = handler.onGeneric(HttpRequest.GET("/api/runtime/sessions/1/answers"), new RuntimeException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());

        ApiErrorResponse body = response.body();
        assertNotNull(body);
        assertEquals("INTERNAL_ERROR", body.code());
        assertEquals("Unexpected server error", body.message());
        assertEquals("/api/runtime/sessions/1/answers", body.path());
    }
}
