package com.id.px3.rest;

import org.springframework.stereotype.Service;

/**
 * Holds the user ID for the current request.
 */
public class UserContextHolder {
    private static final ThreadLocal<String> userIdHolder = new ThreadLocal<>();

    public static void setUserId(String userId) {
        userIdHolder.set(userId);
    }

    public static String getUserId() {
        return userIdHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
    }
}
