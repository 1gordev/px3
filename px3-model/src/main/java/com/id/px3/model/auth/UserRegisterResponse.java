package com.id.px3.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterResponse {
    @Builder.Default
    private Boolean success = false;
    @Builder.Default
    private Boolean alreadyExists = false;
    @Builder.Default
    private String userId = "";
}
