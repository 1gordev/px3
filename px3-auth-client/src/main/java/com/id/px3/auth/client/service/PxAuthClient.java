package com.id.px3.auth.client.service;

import com.id.px3.auth.client.config.Px3AuthClientAppConfig;
import com.id.px3.model.DefaultRoles;
import com.id.px3.model.auth.AuthResponse;
import com.id.px3.model.auth.UserDto;
import com.id.px3.model.auth.UserRegister;
import com.id.px3.model.auth.UserRegisterResponse;
import com.id.px3.rest.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Service
@Slf4j
public class PxAuthClient {

    private static final String PATH_LOGIN = "/token";
    private static final String PATH_REGISTER = "/register";
    private static final String PATH_SET_ACTIVE = "/set-active";
    private static final String REFRESH_TOKEN = "Refresh-Token";
    private static final String PATH_REFRESH_TOKEN = "/token/refresh";
    private final JwtService jwtService;
    private final Px3AuthClientAppConfig appCfg;
    private final RestTemplate restTemplate;

    public PxAuthClient(JwtService jwtService,
                        Px3AuthClientAppConfig appCfg,
                        RestTemplate restTemplate) {
        this.jwtService = jwtService;
        this.appCfg = appCfg;
        this.restTemplate = restTemplate;
    }

    /**
     * Attempt to login
     *
     * @param username - username
     * @param password - password
     * @return Px3 AuthResponse
     */
    public AuthResponse login(String username, String password) {
        var authHeader = "Basic " + java.util.Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
        return login(authHeader);
    }

    /**
     * Attempt to login
     *
     * @param authHeader - basic authentication header
     * @return Px3 AuthResponse
     */
    public AuthResponse login(String authHeader) {
        var url = appCfg.getPx3AuthBaseUrl() + PATH_LOGIN;
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeader);
        HttpEntity<?> requestEntity = new HttpEntity<>(null, headers);
        return restTemplate.postForObject(url, requestEntity, AuthResponse.class);
    }

    /**
     * Refresh the token
     *
     * @param refreshToken - refresh token
     * @return Px3 AuthResponse
     */
    public AuthResponse refresh(String refreshToken) {
        var url = appCfg.getPx3AuthBaseUrl() + PATH_REFRESH_TOKEN;
        var headers = new HttpHeaders();
        headers.set(REFRESH_TOKEN, refreshToken);
        HttpEntity<?> requestEntity = new HttpEntity<>(null, headers);
        return restTemplate.postForObject(url, requestEntity, AuthResponse.class);
    }

    /**
     * Generate a system token for interprocess communication
     *
     * @return a JWT token with ROOT role
     */
    public String generateSystemToken() {
        return jwtService.generateToken(UUID.randomUUID() + "-SSS",
                Set.of(DefaultRoles.ROOT),
                Duration.of(1, ChronoUnit.MINUTES)
        );
    }

    /**
     * Create a new user in the Px3 Auth service.
     *
     * @param username the username of the new user
     * @param password the password of the new user
     * @param roles    the roles assigned to the new user
     * @param active   whether the user is active
     * @param details  additional details for the user
     * @param config   configuration settings for the user
     * @return UserRegisterResponse containing information about the created user
     */
    public UserRegisterResponse registerNewUser(String username,
                                                String password,
                                                List<String> roles,
                                                boolean active,
                                                Map<String, String> details,
                                                Map<String, String> config) {
        var url = appCfg.getPx3UserBaseUrl() + PATH_REGISTER;
        var headers = new HttpHeaders();
        headers.setBearerAuth(generateSystemToken());
        HttpEntity<?> requestEntity = new HttpEntity<>(UserRegister.builder()
                .user(UserDto.builder()
                        .id(UUID.randomUUID().toString())
                        .username(username)
                        .roles(new HashSet<>(roles))
                        .details(details)
                        .config(config)
                        .active(active)
                        .build())
                .password(password)
                .build(),
                headers);
        return restTemplate.postForObject(url, requestEntity, UserRegisterResponse.class);
    }

    public boolean setUserActive(String userId, boolean active) {
        var url = "%s/%s/%s/%s".formatted(appCfg.getPx3UserBaseUrl(), PATH_SET_ACTIVE, userId, active);
        var headers = new HttpHeaders();
        headers.setBearerAuth(generateSystemToken());
        HttpEntity<?> requestEntity = new HttpEntity<>(null, headers);
        try {
            restTemplate.put(url, requestEntity);
            return true;
        } catch (Exception e) {
            log.error("Failed to set user active status", e);
            return false;
        }
    }

    public UserDto findUserById(String userId) {
        var url = "%s/%s".formatted(appCfg.getPx3UserBaseUrl(), userId);
        var headers = new HttpHeaders();
        headers.setBearerAuth(generateSystemToken());
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<UserDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    UserDto.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to find user by ID: {}", userId, e);
            return null;
        }
    }
}
