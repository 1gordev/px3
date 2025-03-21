package com.id.px3.auth.model.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document("users")
public class User {

    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String ENC_PASSWORD = "encPassword";
    public static final String ROLES = "roles";
    public static final String DETAILS = "details";
    public static final String CONFIG = "config";

    public static final String CFG_TOKEN_NEVER_EXPIRES = "tokenNeverExpires";

    @Id
    private String id;

    @Indexed(unique = true)
    @Builder.Default
    private String username = "";

    @Builder.Default
    private String encPassword = "";

    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @Builder.Default
    private Map<String, String> details = new HashMap<>();

    @Builder.Default
    private Map<String, String> config = new HashMap<>();
}
