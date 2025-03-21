package com.id.px3.model.auth;

import lombok.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserDto {
    @Builder.Default
    private String id = "";
    @Builder.Default
    private String username = "";
    @Builder.Default
    private Set<String> roles = new HashSet<>();
    @Builder.Default
    private Map<String, String> config = new HashMap<>();
    @Builder.Default
    private Map<String, String> details = new HashMap<>();
}
