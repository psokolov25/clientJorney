package com.clientjourney.app;

import com.clientjourney.app.dto.ApiErrorResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Produces;
import jakarta.inject.Singleton;

@Singleton
@Produces
public class ApiExceptionHandler {

    @Error(global = true, exception = IllegalArgumentException.class)
    public HttpResponse<ApiErrorResponse> onIllegalArgument(HttpRequest<?> request, IllegalArgumentException ex) {
        return HttpResponse.status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse.of("VALIDATION_ERROR", ex.getMessage(), request.getPath()));
    }

    @Error(global = true)
    public HttpResponse<ApiErrorResponse> onGeneric(HttpRequest<?> request, Throwable ex) {
        return HttpResponse.serverError(ApiErrorResponse.of("INTERNAL_ERROR", "Unexpected server error", request.getPath()));
    }
}
