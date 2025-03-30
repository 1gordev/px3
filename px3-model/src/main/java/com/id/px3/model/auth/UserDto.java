package com.id.px3.model.auth;

import lombok.*;

import java.util.*;

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
    private Boolean active = true;
    @Builder.Default
    private Map<String, String> config = new HashMap<>();
    @Builder.Default
    private Map<String, String> details = new HashMap<>();
    @Builder.Default
    private List<String> indexedProps = new ArrayList<>();
}
