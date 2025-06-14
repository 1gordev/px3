package com.id.px3.rest;

public record PxErrorResponse(
        int status,
        String error,
        String message,
        String path
) {}
