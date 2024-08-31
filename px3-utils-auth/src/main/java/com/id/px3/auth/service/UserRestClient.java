package com.id.px3.auth.service;

import com.id.px3.model.auth.UserDto;
import com.id.px3.model.auth.UserModifyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Objects;


@Service
@Slf4j
public class UserRestClient {

    @Value("${px3.user.url:http://localhost:8080/auth/user}")
    private String userUrl;

    private final RestTemplate restTemplate;

    public UserRestClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getPasswordRules(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                userUrl + "/password-rules",
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Map<String, String>>() {
                }
        );

        return Objects.requireNonNull(response.getBody()).values().stream().findFirst().orElseThrow();
    }

    public UserDto createUser(String authToken, UserModifyRequest userCreate) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        HttpEntity<UserModifyRequest> requestEntity = new HttpEntity<>(userCreate, headers);

        ResponseEntity<UserDto> response = restTemplate.exchange(userUrl, HttpMethod.POST, requestEntity, UserDto.class);
        return response.getBody();
    }

    public UserDto updateUser(String authToken, String userId, UserModifyRequest userModify) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        HttpEntity<UserModifyRequest> requestEntity = new HttpEntity<>(userModify, headers);

        ResponseEntity<UserDto> response = restTemplate.exchange(userUrl + "/" + userId, HttpMethod.PUT, requestEntity, UserDto.class);
        return response.getBody();
    }

    public void deleteUser(String authToken, String pxUserId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        restTemplate.exchange(userUrl + "/" + pxUserId, HttpMethod.DELETE, requestEntity, Void.class);
    }
}
