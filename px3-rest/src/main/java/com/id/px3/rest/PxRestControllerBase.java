package com.id.px3.rest;

import com.id.px3.error.PxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Slf4j
public abstract class PxRestControllerBase {

    public String getAuthToken() {
        return UserContextHolder.getAuthToken();
    }

    public String getUserId() {
        return UserContextHolder.getUserId();
    }

    @ExceptionHandler(PxException.class)
    public final ResponseEntity<Exception> handlePxException(PxException ex, WebRequest request) {
        if(ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            log.error("PxException: %s".formatted(ex.getMessage()));
        } else {
            log.error("PxException: %s".formatted(ex.getMessage()), ex);
        }
        return new ResponseEntity<>(ex, ex.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Exception> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Server error: %s".formatted(ex.getMessage()), ex);
        return new ResponseEntity<>(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
