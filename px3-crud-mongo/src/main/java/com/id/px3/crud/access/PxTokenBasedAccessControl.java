package com.id.px3.crud.access;

import com.id.px3.crud.IPxAccessControlBase;
import com.id.px3.model.DefaultRoles;
import com.id.px3.rest.security.JwtService;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class PxTokenBasedAccessControl<T, K> implements IPxAccessControlBase<T, K> {

    private final Set<String> readRoles;
    private final Set<String> writeRoles;
    private final Set<String> readActions;
    private final Set<String> writeActions;
    private final JwtService jwtService;


    public PxTokenBasedAccessControl(JwtService jwtService,
                                     List<String> readRoles,
                                     List<String> writeRoles,
                                     List<String> readActions,
                                     List<String> writeActions) {
        this.jwtService = jwtService;

        this.readRoles = new HashSet<>(readRoles);
        this.writeRoles = new HashSet<>(writeRoles);
        if(!this.readRoles.isEmpty()) {
            this.readRoles.add(DefaultRoles.ROOT);
        }
        if(!this.writeRoles.isEmpty()) {
            this.writeRoles.add(DefaultRoles.ROOT);
        }

        this.readActions = new HashSet<>(readActions);
        this.writeActions = new HashSet<>(writeActions);
    }

    @Override
    public void canFindAll(String userId, String authToken) {
        if (!readRoles.isEmpty() && noRoleMatch(authToken, readRoles)) {
            var err = "User " + userId + " does not have permission to findAll().";
            log.error(err);
            throw new AccessDeniedException(err);
        }
    }

    @Override
    public void canFindById(String userId, String authToken, K id) {
        if (!readRoles.isEmpty() && noRoleMatch(authToken, readRoles)) {
            var err = "User " + userId + " does not have permission to findById().";
            log.error(err);
            throw new AccessDeniedException(err);
        }
    }

    @Override
    public void canFindByIds(String userId, String authToken, List<K> ids) {
        if (!readRoles.isEmpty() && noRoleMatch(authToken, readRoles)) {
            var err = "User " + userId + " does not have permission to findByIds().";
            log.error(err);
            throw new AccessDeniedException(err);
        }
    }

    @Override
    public void canCreate(String userId, String authToken, T entity) {
        if (!writeRoles.isEmpty() && noRoleMatch(authToken, writeRoles)) {
            var err = "User " + userId + " does not have permission to create.";
            log.error(err);
            throw new AccessDeniedException(err);
        }
    }

    @Override
    public void canUpdate(String userId, String authToken, K id, T entity) {
        if (!writeRoles.isEmpty() && noRoleMatch(authToken, writeRoles)) {
            var err = "User " + userId + " does not have permission to update.";
            log.error(err);
            throw new AccessDeniedException(err);
        }
    }

    @Override
    public void canDelete(String userId, String authToken, K id) {
        if (!writeRoles.isEmpty() && noRoleMatch(authToken, writeRoles)) {
            var err = "User " + userId + " does not have permission to delete.";
            log.error(err);
            throw new AccessDeniedException(err);
        }
    }

    @Override
    public void canDoAction(String userId, String authToken, String name, Map<String, Object> params) {
        if (readActions.contains(name)) {
            if (!readRoles.isEmpty() && noRoleMatch(authToken, readRoles)) {
                var err = "User " + userId + " does not have permission for read action: " + name;
                log.error(err);
                throw new AccessDeniedException(err);
            }
        } else if (writeActions.contains(name)) {
            if (!writeRoles.isEmpty() && noRoleMatch(authToken, writeRoles)) {
                var err = "User " + userId + " does not have permission for write action: " + name;
                log.error(err);
                throw new AccessDeniedException(err);
            }
        } else {
            var err = "Action " + name + " is not allowed.";
            log.error(err);
            throw new AccessDeniedException(err);
        }
    }

    // Helper method to check if the auth token contains any role from the provided set.
    private boolean noRoleMatch(String authToken, Set<String> roles) {
        if (authToken == null) {
            return true;
        }
        try {
            // Validate the token and check for roles
            jwtService.validateTokenWithRoles(authToken, roles);
            // If the token is valid and contains required roles, return false (no access issue)
            return false;
        } catch (Exception e) {
            return true; // Token is invalid or does not contain required roles
        }
    }

    // Custom unchecked exception for access denial.
    public static class AccessDeniedException extends RuntimeException {
        public AccessDeniedException(String message) {
            super(message);
        }
    }
}
