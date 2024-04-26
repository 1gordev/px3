package com.id.px3.auth.rest;

import com.id.px3.auth.model.entity.User;
import com.id.px3.auth.model.entity.UserAccessLog;
import com.id.px3.auth.repo.UserAccessLogRepo;
import com.id.px3.auth.repo.UserRepo;
import com.id.px3.error.PxException;
import com.id.px3.model.RestResponse;
import com.id.px3.model.auth.AuthResponse;
import com.id.px3.model.auth.BasicAuth;
import com.id.px3.model.auth.UserDto;
import com.id.px3.rest.RestControllerBase;
import com.id.px3.rest.RestUtil;
import com.id.px3.rest.security.JwtService;
import com.id.px3.utils.DurationParser;
import com.id.px3.utils.SafeConvert;
import com.id.px3.utils.sec.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("auth")
@Slf4j
public class AuthRest extends RestControllerBase {
    private final UserRepo userRepo;
    private final UserAccessLogRepo userAccessLogRepo;
    private final JwtService jwtService;
    private final Duration accessTokenDuration;
    private final Duration refreshTokenDuration;

    public AuthRest(UserRepo userRepo, UserAccessLogRepo userAccessLogRepo,
                    JwtService jwtService,
                    @Value("${px3.auth.access-token-duration:1h}") String accessTokenDuration,
                    @Value("${px3.auth.refresh-token-duration:12h}") String refreshTokenDuration) {
        this.userRepo = userRepo;
        this.userAccessLogRepo = userAccessLogRepo;
        this.jwtService = jwtService;
        this.accessTokenDuration = DurationParser.parse(accessTokenDuration);
        this.refreshTokenDuration = DurationParser.parse(refreshTokenDuration);
    }

    @GetMapping("ping")
    public RestResponse<String> ping() {
        return RestResponse.ofSuccess("pong");
    }

    /**
     * Attempt to login
     *
     * @param authHeader - basic authentication header
     * @return AuthResponse
     */
    @PostMapping("login")
    public RestResponse<AuthResponse> login(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {

        //  extract username and password from the Basic Auth header
        BasicAuth result = RestUtil.extractBasicAuth(authHeader);

        //  use userRepo to check if the password matches
        User user = userRepo.findByUsername(result.username()).orElseThrow(() -> {
            log.debug("User not found");
            return new PxException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        });

        //  check password
        if (PasswordUtil.matchPassword(result.password(), user.getEncPassword())) {
            //  generate access token duration
            Duration finalAccessTokenDuration;
            Duration finalRefreshTokenDuration;
            if (SafeConvert.toBoolean(user.getConfig().get(User.CFG_TOKEN_NEVER_EXPIRES)).orElse(false)) {
                //  it will be someone else's problem
                finalAccessTokenDuration = Duration.of(1000, ChronoUnit.YEARS);
                finalRefreshTokenDuration = Duration.of(1000, ChronoUnit.YEARS);
            } else {
                finalAccessTokenDuration = accessTokenDuration;
                finalRefreshTokenDuration = refreshTokenDuration;
            }

            //  if it matches, use JwtService to generate an access token and a refresh token
            Instant accessTokenExpiresAt = Instant.now().plus(finalAccessTokenDuration);
            String accessToken = jwtService.generateToken(user.getId(), user.getRoles(), finalAccessTokenDuration);

            Instant refreshTokenExpiresAt = Instant.now().plus(finalRefreshTokenDuration);
            String refreshToken = jwtService.generateToken(user.getId(), Set.of(), finalRefreshTokenDuration);

            //  save the access token and refresh token to the UserAccess entity
            //  this is done asynchronously
            userAccessLogRepo.registerAuthActivityAsync(user.getId(), Map.of(
                    UserAccessLog.LAST_LOGIN, Instant.now(),
                    UserAccessLog.ACCESS_TOKEN_EXPIRE_AT, accessTokenExpiresAt,
                    UserAccessLog.REFRESH_TOKEN_EXPIRE_AT, refreshTokenExpiresAt
            ));

            //  return them via AuthResponse
            AuthResponse authResponse = new AuthResponse(
                    new UserDto(
                            user.getId(),
                            user.getUsername(),
                            user.getRoles(),
                            user.getConfig()
                    ),
                    accessToken,
                    refreshToken,
                    accessTokenExpiresAt.toString(),
                    refreshTokenExpiresAt.toString()
            );
            log.debug("Authentication successful - %s".formatted(authResponse));
            return RestResponse.ofSuccess(authResponse);
        } else {
            log.debug("Password mismatch");
            throw new PxException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }
    }

    @PostMapping("refresh")
    public RestResponse<AuthResponse> refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        //  validate the refresh token
        String subject = jwtService.validateTokenAndGetSubject(refreshToken);

        //  get user
        User user = userRepo.findByUsername(subject).orElseThrow(() -> {
            log.debug("User not found");
            return new PxException(HttpStatus.UNAUTHORIZED, "User not found");
        });

        //  generate a new access token
        Instant accessTokenExpiresAt = Instant.now().plus(accessTokenDuration);
        String newAccessToken = jwtService.generateToken(user.getId(), user.getRoles(), accessTokenDuration);

        //  generate a new refresh token
        Instant refreshTokenExpiresAt = Instant.now().plus(refreshTokenDuration);
        String newRefreshToken = jwtService.generateToken(user.getId(), Set.of(), refreshTokenDuration);

        //  log refresh
        userAccessLogRepo.registerAuthActivityAsync(user.getId(), Map.of(
                UserAccessLog.LAST_REFRESH, Instant.now(),
                UserAccessLog.ACCESS_TOKEN_EXPIRE_AT, accessTokenExpiresAt,
                UserAccessLog.REFRESH_TOKEN_EXPIRE_AT, refreshTokenExpiresAt
        ));

        // Return the new tokens
        AuthResponse authResponse = new AuthResponse(
                new UserDto(
                        user.getId(),
                        user.getUsername(),
                        user.getRoles(),
                        user.getConfig()
                ),
                newAccessToken,
                newRefreshToken,
                accessTokenExpiresAt.toString(),
                refreshTokenExpiresAt.toString()
        );

        log.debug("Token refresh successful - %s".formatted(authResponse));
        return RestResponse.ofSuccess(authResponse);
    }


}
