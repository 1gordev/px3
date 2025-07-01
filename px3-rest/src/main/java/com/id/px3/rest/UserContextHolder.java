package com.id.px3.rest;

import java.util.List;

/**
 * Holds the user ID for the current request.
 */
public class UserContextHolder {
    private static final ThreadLocal<String> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> authTokenHolder = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> rolesHolder = new ThreadLocal<>();

    public static void setAuthToken(String authToken) {
        authTokenHolder.set(authToken);
    }

    public static void setUserId(String userId) {
        userIdHolder.set(userId);
    }

    public static void setRoles(List<String> roles) {
        rolesHolder.set(roles);
    }

    public static String getAuthToken() {
        return authTokenHolder.get();
    }

    public static String getUserId() {
        return userIdHolder.get();
    }

    public static List<String> getRoles() {
        return rolesHolder.get();
    }

    public static void clear() {
        userIdHolder.remove();
    }
}
