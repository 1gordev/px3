package com.id.px3.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PxException extends RuntimeException {

    private HttpStatus statusCode;

    public PxException(HttpStatus statusCode) {
        super(statusCode.getReasonPhrase());
        this.statusCode = statusCode;
    }

    private static String getReasonForCode(int statusCode) {
        return HttpStatus.valueOf(statusCode).getReasonPhrase();
    }

    public PxException(String message) {
        super(message);
    }

    public PxException(HttpStatus statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public PxException(HttpStatus statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
