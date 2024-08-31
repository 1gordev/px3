package com.id.px3.auth.logic;

import com.id.px3.error.PxException;
import com.id.px3.model.DefaultRoles;
import com.id.px3.model.UserConfigValueType;
import com.id.px3.auth.model.entity.User;
import com.id.px3.auth.repo.UserConfigRepo;
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
    private final UserConfigRepo userConfigRepo;
    private final UserRepo userRepo;

    /**
     * Default password rules:
     * At least 8 characters long.
     * Contains at least one uppercase letter (A-Z).
     * Contains at least one lowercase letter (a-z).
     * Contains at least one digit (0-9).
     * Contains at least one special character (e.g., @, #, $, etc.).
     */
    private final Pattern passwordRules;

    public UserModifier(
            UserRoleRepo userRoleRepo,
            UserConfigRepo userConfigRepo,
            UserRepo userRepo,
            @Value("${px3.auth.user.password-rules:^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$}") String passwordRules) {
        this.userRoleRepo = userRoleRepo;
        this.userConfigRepo = userConfigRepo;
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
            var err= "Username already exists: %s".formatted(userCreate.getUsername());
            log.debug(err);
            throw new IllegalArgumentException(err);
        }

        //  validate requested roles
        validateRequestedRoles(userCreate);

        //  validate configs
        validateConfigs(userCreate);

        //  create user
        User newUser = new User(
                UUID.randomUUID().toString(),
                userCreate.getUsername(),
                PasswordUtil.encodePassword(userCreate.getPassword()),
                userCreate.getRoles(),
                userCreate.getConfig()
        );
        log.debug("New user created: %s".formatted(newUser));

        //  persist and return userDto
        newUser = userRepo.save(newUser);
        return new UserDto(newUser.getId(), newUser.getUsername(), newUser.getRoles(), newUser.getConfig());
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
        validateConfigs(userModify);

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
        return new UserDto(user.getId(), user.getUsername(), user.getRoles(), user.getConfig());
    }

    /**
     * Delete an existing user
     *
     * @param userId - user id
     */
    public void delete(String userId) {
        userRepo.deleteById(userId);
    }

    private void validateConfigs(UserModifyRequest userCreate) {
        if (userCreate.getConfig() != null && !userCreate.getConfig().isEmpty()) {
            userCreate.getConfig().forEach((cfgKey, cfgVal) -> {
                userConfigRepo.findByCode(cfgKey).ifPresentOrElse(
                        userConfig -> {
                            //  validate config value
                            if (cfgVal == null) {
                                var err = "Config value cannot be null: %s".formatted(cfgKey);
                                log.debug(err);
                                throw new IllegalArgumentException(err);
                            } else {
                                //  validate config value type
                                if (!validateValueType(userConfig.getValueType(), cfgVal)) {
                                    var err = "Invalid config value type: %s".formatted(cfgKey);
                                    log.debug(err);
                                    throw new IllegalArgumentException(err);
                                }
                            }
                        },
                        () -> {
                            var err = "Config not found: %s".formatted(cfgKey);
                            log.debug(err);
                            throw new IllegalArgumentException(err);
                        }
                );
            });
        }
    }

    private void validateRequestedRoles(UserModifyRequest userCreate) {
        if (userCreate.getRoles() != null && !userCreate.getRoles().isEmpty()) {

            boolean isDefaultRole = userCreate.getRoles().stream().anyMatch(role -> DefaultRoles.getRoles().contains(role));
            boolean isConfiguredRole = userCreate.getRoles().stream().anyMatch(role -> userRoleRepo.findByCode(role).isPresent());

            if(!isDefaultRole && !isConfiguredRole) {
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
