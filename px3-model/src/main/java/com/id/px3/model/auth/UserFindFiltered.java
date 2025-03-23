package com.id.px3.model.auth;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class UserFindFiltered {

    @Builder.Default
    private List<String> ids = new ArrayList<>();
    @Builder.Default
    private List<String> roles = new ArrayList<>();
    @Builder.Default
    private List<String> indexedProps = new ArrayList<>();

}
