package com.id.px3.rest;

import com.id.px3.error.PxException;
import com.id.px3.model.RestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestControllerAdvice
public abstract class RestControllerBase {

    private ThreadLocal<String> userId = new ThreadLocal<>();

    public String getUserId() {
        return userId.get();
    }

    public void setUserId(String userId) {
        this.userId.set(userId);
    }

    @ExceptionHandler(PxException.class)
    public final ResponseEntity<RestResponse<Void>> handlePxException(PxException ex, WebRequest request) {
        RestResponse<Void> response = RestResponse.ofError(
                ex.getStatusCode(),
                ex.getMessage(),
                Collections.singletonList(ex.getMessage() + " " + Stream.of(ex.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(Collectors.joining(", ")))
        );
        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<RestResponse<Void>> handleAllExceptions(Exception ex, WebRequest request) {
        // Utilize the ofError static method for generic exceptions
        RestResponse<Void> response = RestResponse.ofError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                Collections.singletonList(ex.getMessage() + " " + Stream.of(ex.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(Collectors.joining(", ")))
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
