package com.id.px3.auth.model.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Document("users")
@CompoundIndex(name = "id_roles_idx", def = "{'id': 1, 'roles': 1}")
@CompoundIndex(name = "id_indexedProps_idx", def = "{'id':1, 'indexedProps':1}")
public class User {

    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String ENC_PASSWORD = "encPassword";
    public static final String ROLES = "roles";
    public static final String DETAILS = "details";
    public static final String CONFIG = "config";
    public static final String ACTIVE = "active";
    public static final String INDEXED_PROPS = "indexedProps";

    public static final String CFG_TOKEN_NEVER_EXPIRES = "tokenNeverExpires";

    @Id
    private String id;

    @Indexed(unique = true)
    @Builder.Default
    private String username = "";

    @Builder.Default
    private String encPassword = "";

    @Builder.Default
    @Indexed
    private Set<String> roles = new HashSet<>();

    @Builder.Default
    private Boolean active = true;

    @Builder.Default
    private Map<String, String> details = new HashMap<>();

    @Builder.Default
    private Map<String, String> config = new HashMap<>();

    @Builder.Default
    @Indexed
    private List<String> indexedProps = new ArrayList<>();
}
