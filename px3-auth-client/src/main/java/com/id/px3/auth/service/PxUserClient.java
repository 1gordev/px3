package com.id.px3.auth.service;

import com.id.px3.model.auth.UserDto;
import com.id.xmove.config.AppConfig;
import com.id.xmove.module.auth.model.XmUserFindFiltered;
import com.id.xmove.module.auth.model.XmUserModifyRequest;
import com.id.xmove.module.auth.model.factory.XmUserFindFilteredFactory;
import com.id.xmove.module.auth.model.factory.XmUserModifyRequestFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class PxUserClient {

    public static final String USER_FIND_FILTERED = "/user/find-filtered";
    public static final String USER = "/user";
    public static final String USER_PASSWORD_RULES = "/user/password-rules";
    public static final String USER_PASSWORD = "/user/password";

    private final AppConfig appConfig;
    private final RestTemplate restTemplate;

    public PxUserClient(AppConfig appConfig, RestTemplate restTemplate) {
        this.appConfig = appConfig;
        this.restTemplate = restTemplate;
    }

    public List<UserDto> findFiltered(String authToken, XmUserFindFiltered findFilteredReq) {
        ResponseEntity<List<UserDto>> response = restTemplate.exchange(
                "%s%s".formatted(appConfig.getPx3AuthBaseUrl(), USER_FIND_FILTERED),
                HttpMethod.POST,
                buildRequestEntity(authToken, XmUserFindFilteredFactory.toUserFindFiltered(findFilteredReq)),
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }

    public Optional<UserDto> findById(String authToken, String id) {
        ResponseEntity<UserDto> response = restTemplate.exchange(
                "%s%s".formatted(appConfig.getPx3AuthBaseUrl(), USER),
                HttpMethod.POST,
                buildRequestEntity(authToken, null),
                new ParameterizedTypeReference<>() {
                }
        );
        return Optional.ofNullable(response.getBody());
    }

    public Optional<Map<String,String>> getPasswordRules(String authToken) {
        ResponseEntity<Map<String,String>> response = restTemplate.exchange(
                "%s%s".formatted(appConfig.getPx3AuthBaseUrl(), USER_PASSWORD_RULES),
                HttpMethod.GET,
                buildRequestEntity(authToken, null),
                new ParameterizedTypeReference<>() {
                }
        );
        return Optional.ofNullable(response.getBody());
    }

    public Optional<UserDto> create(String authToken, XmUserModifyRequest userCreate) {
        ResponseEntity<UserDto> response = restTemplate.exchange(
                "%s%s".formatted(appConfig.getPx3AuthBaseUrl(), USER),
                HttpMethod.POST,
                buildRequestEntity(authToken, XmUserModifyRequestFactory.toUserModifyRequest(userCreate)),
                new ParameterizedTypeReference<>() {
                }
        );
        return Optional.ofNullable(response.getBody());
    }

    public Optional<UserDto> update(String authToken, String userId, XmUserModifyRequest userCreate) {
        ResponseEntity<UserDto> response = restTemplate.exchange(
                "%s%s/%s".formatted(appConfig.getPx3AuthBaseUrl(), USER, userId),
                HttpMethod.PUT,
                buildRequestEntity(authToken, XmUserModifyRequestFactory.toUserModifyRequest(userCreate)),
                new ParameterizedTypeReference<>() {
                }
        );
        return Optional.ofNullable(response.getBody());
    }

    public void delete(String authToken, String userId) {
        restTemplate.exchange(
                "%s%s/%s".formatted(appConfig.getPx3AuthBaseUrl(), USER, userId),
                HttpMethod.DELETE,
                buildRequestEntity(authToken, null),
                new ParameterizedTypeReference<>() {
                }
        );
    }

    public Optional<UserDto> changePassword(String authToken, String id, String newPassword) {
        ResponseEntity<UserDto> response = restTemplate.exchange(
                "%s%s/%s".formatted(appConfig.getPx3AuthBaseUrl(), USER_PASSWORD, id),
                HttpMethod.PUT,
                buildRequestEntity(authToken, newPassword),
                new ParameterizedTypeReference<>() {
                }
        );
        return Optional.ofNullable(response.getBody());
    }

    private static <T> HttpEntity<T> buildRequestEntity(String authToken, T body) {
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(authToken));
        return new HttpEntity<>(body, headers);
    }

}
