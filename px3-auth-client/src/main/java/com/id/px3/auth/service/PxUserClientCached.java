package com.id.px3.auth.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.id.px3.model.auth.UserDto;
import com.id.xmove.config.AppConfig;
import com.id.xmove.module.auth.model.XmUserFindFiltered;
import com.id.xmove.module.auth.model.XmUserModifyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PxUserClientCached extends PxUserClient {

    private final Cache<String, UserDto> findByIdCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build();

    private final Cache<String, List<UserDto>> findFilteredCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build();

    private final Cache<String, Map<String, String>> passwordRulesCache = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.SECONDS)
            .build();

    public PxUserClientCached(AppConfig appConfig, RestTemplate restTemplate) {
        super(appConfig, restTemplate);
    }

    @Override
    public List<UserDto> findFiltered(String authToken, XmUserFindFiltered findFilteredReq) {
        // Use a cache key based on authToken and a string representation of the filter request.
        String key = authToken + "_" + findFilteredReq.toString();
        List<UserDto> cached = findFilteredCache.getIfPresent(key);
        if (cached != null) {
            log.trace("Cache hit for findFiltered: {}", key);
            return cached;
        }
        log.trace("Cache miss for findFiltered: {}", key);
        List<UserDto> result = super.findFiltered(authToken, findFilteredReq);
        findFilteredCache.put(key, result);
        return result;
    }

    @Override
    public Optional<UserDto> findById(String authToken, String id) {
        String key = authToken + "_" + id;
        UserDto cached = findByIdCache.getIfPresent(key);
        if (cached != null) {
            log.trace("Cache hit for findById: {}", key);
            return Optional.of(cached);
        }
        log.trace("Cache miss for findById: {}", key);
        Optional<UserDto> result = super.findById(authToken, id);
        result.ifPresent(user -> findByIdCache.put(key, user));
        return result;
    }

    public Collection<Object> findByRole(String token, String role) {
        return null;
    }

    @Override
    public Optional<Map<String, String>> getPasswordRules(String authToken) {
        // Assume that password rules do not vary per user, so use authToken as key.
        String key = authToken;
        Map<String, String> cached = passwordRulesCache.getIfPresent(key);
        if (cached != null) {
            log.trace("Cache hit for getPasswordRules: {}", key);
            return Optional.of(cached);
        }
        log.trace("Cache miss for getPasswordRules: {}", key);
        Optional<Map<String, String>> result = super.getPasswordRules(authToken);
        result.ifPresent(rules -> passwordRulesCache.put(key, rules));
        return result;
    }

    // Write operations clear the caches to ensure that the cached data remains consistent.
    @Override
    public Optional<UserDto> create(String authToken, XmUserModifyRequest userCreate) {
        Optional<UserDto> result = super.create(authToken, userCreate);
        invalidateCaches();
        return result;
    }

    @Override
    public Optional<UserDto> update(String authToken, String userId, XmUserModifyRequest userCreate) {
        Optional<UserDto> result = super.update(authToken, userId, userCreate);
        invalidateCaches();
        return result;
    }

    @Override
    public void delete(String authToken, String userId) {
        super.delete(authToken, userId);
        invalidateCaches();
    }


    /**
     * Invalidate all caches.
     */
    private void invalidateCaches() {
        log.trace("Invalidating caches due to write operation");
        findByIdCache.invalidateAll();
        findFilteredCache.invalidateAll();
        passwordRulesCache.invalidateAll();
    }

}
