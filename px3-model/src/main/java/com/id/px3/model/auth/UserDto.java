package com.id.px3.model.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDto {
    private String id = "";
    private String username = "";
    private Set<String> roles = new HashSet<>();
    private Map<String, Object> config = new HashMap<>();
}
