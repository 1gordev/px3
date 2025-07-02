package com.id.px3.auth.rest;

import com.id.px3.auth.logic.UserFinder;
import com.id.px3.auth.logic.UserModifier;
import com.id.px3.model.DefaultRoles;
import com.id.px3.auth.model.entity.User;
import com.id.px3.auth.repo.UserRepo;
import com.id.px3.error.PxException;
import com.id.px3.model.auth.*;
import com.id.px3.rest.PxRestControllerBase;
import com.id.px3.rest.security.JwtSecured;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("user")
@Slf4j
public class UserPxRest extends PxRestControllerBase {

    private final ApplicationContext appCtx;
    private final UserRepo userRepo;

    public UserPxRest(ApplicationContext appCtx, UserRepo userRepo) {
        this.appCtx = appCtx;
        this.userRepo = userRepo;
    }

    @GetMapping("{userId}")
    @JwtSecured(roles = {DefaultRoles.ROOT, DefaultRoles.USERS_LIST})
    public UserDto findById(@PathVariable String userId) {
        return appCtx.getBean(UserFinder.class).findById(userId);
    }

    @PostMapping("find-filtered")
    @JwtSecured(roles = {DefaultRoles.ROOT, DefaultRoles.USERS_LIST})
    public List<UserDto> findFiltered(@RequestBody UserFindFiltered findFilteredReq) {
        return appCtx.getBean(UserFinder.class).findFiltered(findFilteredReq);
    }

    @GetMapping("password-rules")
    @JwtSecured
    public Map<String,String> getPasswordRules() {
        return Map.of("value", appCtx.getBean(UserModifier.class).getPasswordRules());
    }

    @PostMapping
    @JwtSecured(roles = {DefaultRoles.ROOT, DefaultRoles.USERS_WRITE})
    public UserDto create(@RequestBody UserModifyRequest userCreate) {
        return appCtx.getBean(UserModifier.class).create(userCreate);
    }

    @PostMapping("register")
    @JwtSecured(roles = {DefaultRoles.ROOT, DefaultRoles.USERS_WRITE})
    public UserRegisterResponse register(@RequestBody UserRegister userRegister) {
        return appCtx.getBean(UserModifier.class).register(userRegister);
    }

    @PutMapping("set-active/{userId}/{active}")
    @JwtSecured(roles = {DefaultRoles.ROOT, DefaultRoles.USERS_WRITE})
    public UserDto setActive(@PathVariable String userId, @PathVariable boolean active) {
        appCtx.getBean(UserModifier.class).setActive(userId, active);
        return appCtx.getBean(UserFinder.class).findById(userId);
    }

    @PutMapping("{userId}")
    @JwtSecured
    public UserDto update(@PathVariable String userId, @RequestBody UserModifyRequest userModify) {
        //  retrieve current user
        User requesingUser = userRepo.findById(getUserId()).orElseThrow();
        //  allow only ROOT or the user itself to update
        if (!requesingUser.getId().equals(userId)
                && !requesingUser.getRoles().contains(DefaultRoles.ROOT)
                && !requesingUser.getRoles().contains(DefaultRoles.USERS_WRITE)) {
            log.debug("Current user id %s cannot update user id %s".formatted(requesingUser.getId(), userId));
            throw new PxException(HttpStatus.BAD_REQUEST, "Unauthorized to perform this action");
        }

        return appCtx.getBean(UserModifier.class).update(userId, userModify);
    }

    @DeleteMapping("{userId}")
    @JwtSecured
    public void delete(@PathVariable String userId) {
        //  retrieve current user
        User requesingUser = userRepo.findById(getUserId()).orElseThrow();
        //  allow only ROOT or the user itself to delete
        if (!requesingUser.getId().equals(userId)
                && !requesingUser.getRoles().contains(DefaultRoles.ROOT)
                && !requesingUser.getRoles().contains(DefaultRoles.USERS_WRITE)) {
            log.debug("Current user id %s cannot delete user id %s".formatted(requesingUser.getId(), userId));
            throw new PxException(HttpStatus.BAD_REQUEST, "Unauthorized to perform this action");
        }

        appCtx.getBean(UserModifier.class).delete(userId);
    }
}
