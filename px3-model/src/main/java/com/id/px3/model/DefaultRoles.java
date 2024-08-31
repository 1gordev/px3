package com.id.px3.model;

import java.util.List;
import java.util.Map;

public final class DefaultRoles {
    public static final String ROOT = "ROOT";
    public static final String USERS_WRITE = "USERS_WRITE";

    private static final Map<String, String> descriptions = Map.of(
            ROOT, "Root role",
            USERS_WRITE, "Can write to users table"
    );

    public static String getDescription(String role) {
        return descriptions.get(role);
    }
    public static List<String> getRoles() {
        return List.of(ROOT, USERS_WRITE);
    }
}
