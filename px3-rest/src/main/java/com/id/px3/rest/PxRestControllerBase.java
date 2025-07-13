package com.id.px3.rest;

import com.id.px3.error.PxException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@RestControllerAdvice
@Slf4j
public abstract class PxRestControllerBase {

    public String getAuthToken() {
        return UserContextHolder.getAuthToken();
    }

    public String getUserId() {
        return UserContextHolder.getUserId();
    }

    public List<String> getRoles() {
        return UserContextHolder.getRoles();
    }

    @ExceptionHandler(PxException.class)
    public final ResponseEntity<PxErrorResponse> handlePxException(PxException ex, WebRequest request) {
        HttpStatus status = ex.getStatusCode();
        String path = request.getDescription(false).replace("uri=", "");
        PxErrorResponse body = new PxErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                path
        );
        if(ex.getStatusCode() == HttpStatus.UNAUTHORIZED && ex.getMessage().contains("Token has expired")) {
            log.debug("PxException: Unauthorized access due to expired token: {}", ex.getMessage());
        } else {
            log.error("PxException: {}", ex.getMessage(), ex);
        }
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<PxErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String path = request.getDescription(false).replace("uri=", "");
        PxErrorResponse body = new PxErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                path
        );
        log.error("Server error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(body, status);
    }

}

