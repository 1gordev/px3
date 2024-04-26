package com.id.px3.model.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private UserDto user;
    private String accessToken;
    private String refreshToken;
    private String accessTokenExpiresAt;
    private String refreshTokenExpiresAt;
}
