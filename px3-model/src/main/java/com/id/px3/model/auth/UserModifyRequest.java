package com.id.px3.model.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserModifyRequest {
    private String username;
    private String password;
    private Set<String> roles;
    private Map<String, Object> config;
}
