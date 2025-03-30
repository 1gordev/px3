package com.id.px3.auth.logic;

import com.id.px3.model.DefaultRoles;
import com.id.px3.model.UserConfigValueType;
import com.id.px3.auth.model.entity.User;
import com.id.px3.auth.repo.UserRepo;
import com.id.px3.auth.repo.UserRoleRepo;
import com.id.px3.model.auth.UserModifyRequest;
import com.id.px3.model.auth.UserDto;
import com.id.px3.utils.SafeConvert;
import com.id.px3.utils.sec.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Pattern;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Slf4j
public class UserModifier {

    private final UserRoleRepo userRoleRepo;
    private final UserRepo userRepo;

    /**
     * Default password rules:
     * At least 8 characters long.
     * Contains at least one uppercase letter (A-Z).
     * Contains at least one lowercase letter (a-z).
     * Contains at least one digit (0-9).
     * Contains at least one special character (e.g., @, #, $, etc.).
     * In alternative, a 6 digit pin is accepted
     */
    private final Pattern passwordRules;

    public UserModifier(
            UserRoleRepo userRoleRepo,
            UserRepo userRepo,
            @Value("${px3.auth.user.password-rules:^(?:(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}|\\d{6})$}") String passwordRules) {
        this.userRoleRepo = userRoleRepo;
        this.userRepo = userRepo;
        this.passwordRules = Pattern.compile(passwordRules);
    }

    public String getPasswordRules() {
        return passwordRules.pattern();
    }

    /**
     * Create a new user
     *
     * @param userCreate - user creation request
     * @return The created user
     */
    public UserDto create(UserModifyRequest userCreate) {

        //  validate credentials
        if (userCreate.getUsername() == null || userCreate.getUsername().trim().isEmpty()) {
            var err = "Username cannot be empty";
            log.debug(err);
            throw new IllegalArgumentException(err);
        }

        if (userCreate.getPassword() == null || userCreate.getPassword().trim().isEmpty()) {
            var err = "Password cannot be empty";
            log.debug(err);
            throw new IllegalArgumentException(err);
        }

        //  check password rules
        if (!passwordRules.matcher(userCreate.getPassword()).matches()) {
            var err = "Password does not meet the required rules";
            log.debug(err);
            throw new IllegalArgumentException(err);
        }

        //  check if username already exists
        if (userRepo.findByUsername(userCreate.getUsername()).isPresent()) {
            var err = "Username already exists: %s".formatted(userCreate.getUsername());
            log.debug(err);
            throw new IllegalArgumentException(err);
        }

        //  validate requested roles
        validateRequestedRoles(userCreate);

        //  validate configs
        validateConfigsAndDetails(userCreate);

        //  create user
        var newUser = User.builder()
                .id(UUID.randomUUID().toString())
                .username(userCreate.getUsername())
                .encPassword(PasswordUtil.encodePassword(userCreate.getPassword()))
                .roles(userCreate.getRoles())
                .active(userCreate.getActive())
                .config(userCreate.getConfig())
                .details(userCreate.getDetails())
                .build();
        log.debug("New user created: %s".formatted(newUser));

        //  persist and return userDto
        newUser = userRepo.save(newUser);
        return UserDto.builder()
                .id(newUser.getId())
                .username(newUser.getUsername())
                .roles(newUser.getRoles())
                .config(newUser.getConfig())
                .details(newUser.getDetails())
                .build();
    }

    /**
     * Update an existing user
     *
     * @param userId - user id
     * @param userModify - user modification request
     * @return The updated user
     */
    public UserDto update(String userId, UserModifyRequest userModify) {
        //  validate credentials
        if (userModify.getUsername() == null || userModify.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        //  plain password can be null or void, in this case the password will not be updated
        if (userModify.getPassword() != null && !userModify.getPassword().trim().isEmpty()) {
            //  check password rules
            if (!passwordRules.matcher(userModify.getPassword()).matches()) {
                throw new IllegalArgumentException("Password does not meet the required rules");
            }
        }

        //  validate requested roles
        validateRequestedRoles(userModify);

        //  validate configs
        validateConfigsAndDetails(userModify);

        //  retrieve user
        User user = userRepo.findById(userId).orElseThrow();

        //  update password if not null or empty
        if (userModify.getPassword() != null && !userModify.getPassword().trim().isEmpty()) {
            user.setEncPassword(PasswordUtil.encodePassword(userModify.getPassword()));
        }

        //  update roles and config
        user.setRoles(userModify.getRoles());
        user.setConfig(userModify.getConfig());
        log.debug("User modified: %s".formatted(user));

        //  persist and return userDto
        user = userRepo.save(user);
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .roles(user.getRoles())
                .config(user.getConfig())
                .details(user.getDetails())
                .build();
    }

    /**
     * Delete an existing user
     *
     * @param userId - user id
     */
    public void delete(String userId) {
        userRepo.deleteById(userId);
    }

    private void validateConfigsAndDetails(UserModifyRequest userCreate) {
        // Limit config and details to 16 keys
        if (userCreate.getConfig().size() > 16) {
            var err = "Config cannot have more than 16 keys";
            log.debug(err);
            throw new IllegalArgumentException(err);
        }
        if (userCreate.getDetails().size() > 16) {
            var err = "Details cannot have more than 16 keys";
            log.debug(err);
            throw new IllegalArgumentException(err);
        }


        // Don't allow null values, limit keys to 64 chars and values to 256 chars
        for (String key : userCreate.getConfig().keySet()) {
            if (key.length() > 64) {
                var err = "Config key cannot be longer than 64 characters: %s".formatted(key);
                log.debug(err);
                throw new IllegalArgumentException(err);
            }

            String value = userCreate.getConfig().get(key);
            if (value == null) {
                var err = "Config value cannot be null: %s".formatted(key);
                log.debug(err);
                throw new IllegalArgumentException(err);
            }
            if (value.length() > 256) {
                var err = "Config value cannot be longer than 256 characters: %s".formatted(key);
                log.debug(err);
                throw new IllegalArgumentException(err);
            }
        }

        for (String key : userCreate.getDetails().keySet()) {
            if (key.length() > 64) {
                var err = "Detail key cannot be longer than 64 characters: %s".formatted(key);
                log.debug(err);
                throw new IllegalArgumentException(err);
            }

            String value = userCreate.getDetails().get(key);
            if (value == null) {
                var err = "Detail value cannot be null: %s".formatted(key);
                log.debug(err);
                throw new IllegalArgumentException(err);
            }
            if (value.length() > 256) {
                var err = "Detail value cannot be longer than 256 characters: %s".formatted(key);
                log.debug(err);
                throw new IllegalArgumentException(err);
            }
        }
    }

    private void validateRequestedRoles(UserModifyRequest userCreate) {
        if (userCreate.getRoles() != null && !userCreate.getRoles().isEmpty()) {

            boolean isDefaultRole = userCreate.getRoles().stream().anyMatch(role -> DefaultRoles.getRoles().contains(role));
            boolean isConfiguredRole = userCreate.getRoles().stream().anyMatch(role -> userRoleRepo.findByCode(role).isPresent());

            if (!isDefaultRole && !isConfiguredRole) {
                var err = "Role not found: %s".formatted(userCreate.getRoles());
                log.debug(err);
                throw new IllegalArgumentException(err);
            }
        }
    }


    private boolean validateValueType(UserConfigValueType valueType, Object cfgVal) {
        return switch (valueType) {
            case STRING -> SafeConvert.toString(cfgVal).isPresent();
            case LONG -> SafeConvert.toLong(cfgVal).isPresent();
            case BOOLEAN -> SafeConvert.toBoolean(cfgVal).isPresent();
            case DOUBLE -> SafeConvert.toDouble(cfgVal).isPresent();
        };
    }

}
