package com.id.px3.model;

import java.util.Map;

public final class DefaultRoles {
    public static final String ROOT = "ROOT";
    public static final String USERS_WRITE = "USERS_WRITE";
    private final Map<String, String> descriptions = Map.of(
            ROOT, "Root role",
            USERS_WRITE, "Can write to users table"
    );

    public String getDescription(String role) {
        return descriptions.get(role);
    }

}
