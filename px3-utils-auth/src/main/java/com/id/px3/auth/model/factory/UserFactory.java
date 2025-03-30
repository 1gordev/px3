package com.id.px3.auth.model.factory;

import com.id.px3.auth.model.entity.User;
import com.id.px3.model.auth.UserDto;

public class UserFactory {

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .roles(user.getRoles())
                .active(user.getActive())
                .config(user.getConfig())
                .details(user.getDetails())
                .indexedProps(user.getIndexedProps())
                .build();
    }

}
