package com.id.px3.auth.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "user_access_log")
public class UserAccessLog {

    public final static String ID = "id";
    public final static String USER_ID = "userId";

    public final static String LAST_LOGIN = "lastLogin";
    public final static String LAST_REFRESH = "lastRefresh";
    public final static String LAST_LOGOUT = "lastLogout";
    public final static String ACCESS_TOKEN_EXPIRE_AT = "accessTokenExpireAt";
    public final static String REFRESH_TOKEN_EXPIRE_AT = "refreshTokenExpireAt";

    @Id
    @Field("_id")
    private String id;

    @Indexed(unique = true)
    private String userId;
    private Instant lastLogin;
    private Instant lastRefresh;
    private Instant lastLogout;
    private Instant accessTokenExpireAt;
    private Instant refreshTokenExpireAt;
}
