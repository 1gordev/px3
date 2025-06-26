package com.id.px3.auth.client.service;

import com.id.px3.auth.client.config.Px3AuthClientAppConfig;
import com.id.px3.model.auth.AuthResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
@Slf4j
public class PxAuthClient {

    private static final String PATH_LOGIN = "/token";
    private static final String REFRESH_TOKEN = "Refresh-Token";
    private static final String PATH_REFRESH_TOKEN = "/token/refresh";
    private final Px3AuthClientAppConfig px3AuthClientAppConfig;
    private final RestTemplate restTemplate;

    public PxAuthClient(Px3AuthClientAppConfig px3AuthClientAppConfig, RestTemplate restTemplate) {
        this.px3AuthClientAppConfig = px3AuthClientAppConfig;
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
        var url = px3AuthClientAppConfig.getPx3AuthBaseUrl() + PATH_LOGIN;
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
        var url = px3AuthClientAppConfig.getPx3AuthBaseUrl() + PATH_REFRESH_TOKEN;
        var headers = new HttpHeaders();
        headers.set(REFRESH_TOKEN, refreshToken);
        HttpEntity<?> requestEntity = new HttpEntity<>(null, headers);
        return restTemplate.postForObject(url, requestEntity, AuthResponse.class);
    }

}
