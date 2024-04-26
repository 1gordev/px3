package com.id.px3.model;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RestResponse<T> {
    private T response;
    private int statusCode;
    private String message;
    private List<String> systemError;

    public static <T> RestResponse<T> ofSuccess(T response) {
        return new RestResponse<>(response, HttpStatus.OK.value(), null, null);
    }
    public static <T> RestResponse<T> ofSuccess(HttpStatus statusCode, T response) {
        return new RestResponse<>(response, statusCode.value(), null, null);
    }
    public static RestResponse<Void> ofError(HttpStatus statusCode, String message) {
        return new RestResponse<>(null, statusCode.value(), message, null);
    }
    public static RestResponse<Void> ofError(HttpStatus statusCode, String message, List<String> systemError) {
        return new RestResponse<>(null, statusCode.value(), message, systemError);
    }
}
