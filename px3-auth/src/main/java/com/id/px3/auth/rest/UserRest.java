package com.id.px3.auth.rest;

import com.id.px3.auth.logic.UserModifier;
import com.id.px3.auth.model.DefaultRoles;
import com.id.px3.auth.model.entity.User;
import com.id.px3.auth.repo.UserRepo;
import com.id.px3.error.PxException;
import com.id.px3.model.auth.UserModifyRequest;
import com.id.px3.model.auth.UserDto;
import com.id.px3.rest.security.JwtSecured;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user")
@Slf4j
public class UserRest {

    private final ApplicationContext appCtx;
    private final UserRepo userRepo;

    public UserRest(ApplicationContext appCtx, UserRepo userRepo) {
        this.appCtx = appCtx;
        this.userRepo = userRepo;
    }

    @PostMapping
    @JwtSecured(roles = {DefaultRoles.ROOT, DefaultRoles.USERS_WRITE})
    public UserDto create(@RequestBody UserModifyRequest userCreate) {
        return appCtx.getBean(UserModifier.class).create(userCreate);
    }

    @PutMapping("{userId}")
    @JwtSecured
    public UserDto update(@PathVariable String userId, @RequestBody UserModifyRequest userModify) {
        //  retrieve current user
        User currentUser = userRepo.findById(userId).orElseThrow();
        //  allow only ROOT or the user itself to update
        if (!currentUser.getId().equals(userId)
                && !currentUser.getRoles().contains(DefaultRoles.ROOT)
                && !currentUser.getRoles().contains(DefaultRoles.USERS_WRITE)) {
            log.debug("Current user id %s cannot update user id %s".formatted(currentUser.getId(), userId));
            throw new PxException(HttpStatus.BAD_REQUEST, "Unauthorized to perform this action");
        }

        return appCtx.getBean(UserModifier.class).update(userId, userModify);
    }
}
