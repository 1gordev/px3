package com.id.px3.auth.repo;

import com.id.px3.auth.model.entity.UserAccessLog;
import com.id.px3.utils.mongo.IndexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@Slf4j
public class UserAccessLogRepo {

    private final MongoTemplate mongoTemplate;

    public UserAccessLogRepo(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void init() {
        IndexUtils.ensureIndexes(mongoTemplate, UserAccessLog.class);
        log.info("UserAccessRepo initialized");
    }


    /**
     * Register a set of authentication activities for the given user.
     * This method is asynchronous.
     *
     * @param userId      user id
     * @param fieldValues field values to update
     */
    @Async
    public synchronized void registerAuthActivityAsync(String userId, Map<String, Instant> fieldValues) {
        try {
            if (fieldValues != null && !fieldValues.isEmpty()) {
                //  retrieve access entity
                UserAccessLog access = mongoTemplate.findOne(query(where(UserAccessLog.USER_ID).is(userId)), UserAccessLog.class);
                if (access == null) {
                    access = new UserAccessLog();
                    access.setId(UUID.randomUUID().toString());
                    access.setUserId(userId);
                }

                //  update field
                UserAccessLog finalAccess = access;
                fieldValues.forEach((field, value) -> {
                    switch (field) {
                        case UserAccessLog.LAST_LOGIN -> finalAccess.setLastLogin(value);
                        case UserAccessLog.LAST_REFRESH -> finalAccess.setLastRefresh(value);
                        case UserAccessLog.LAST_LOGOUT -> finalAccess.setLastLogout(value);
                        case UserAccessLog.ACCESS_TOKEN_EXPIRE_AT -> finalAccess.setAccessTokenExpireAt(value);
                        case UserAccessLog.REFRESH_TOKEN_EXPIRE_AT -> finalAccess.setRefreshTokenExpireAt(value);
                        default -> log.error(String.format("Unknown field '%s' for UserAccess", field));
                    }
                });

                //  save
                mongoTemplate.save(access);
            }
        } catch (Exception e) {
            log.error(String.format("Error registering auth activity: %s", e.getMessage()));
        }
    }

}
