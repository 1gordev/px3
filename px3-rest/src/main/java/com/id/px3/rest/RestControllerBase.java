package com.id.px3.rest;

import com.id.px3.error.PxException;
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

    public String getAuthToken() {
        return UserContextHolder.getAuthToken();
    }

    public String getUserId() {
        return UserContextHolder.getUserId();
    }

    @ExceptionHandler(PxException.class)
    public final ResponseEntity<Exception> handlePxException(PxException ex, WebRequest request) {
        return new ResponseEntity<>(ex, ex.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Exception> handleAllExceptions(Exception ex, WebRequest request) {
        // Utilize the ofError static method for generic exceptions
        return new ResponseEntity<>(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
