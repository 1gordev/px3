package com.id.px3.auth.repo;

import com.id.px3.auth.model.entity.User;
import com.id.px3.auth.model.entity.UserAccessLog;
import com.id.px3.utils.mongo.IndexUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

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
                UserAccessLog access = mongoTemplate.findById(userId, UserAccessLog.class);
                if (access == null) {
                    access = new UserAccessLog();
                    access.setUserId(userId);
                }

                //  update field
                UserAccessLog finalAccess = access;
                fieldValues.forEach((field, value) -> {
                    switch (field) {
                        case UserAccessLog.LAST_LOGIN:
                            finalAccess.setLastLogin(value);
                            break;
                        case UserAccessLog.LAST_REFRESH:
                            finalAccess.setLastRefresh(value);
                            break;
                        case UserAccessLog.LAST_LOGOUT:
                            finalAccess.setLastLogout(value);
                            break;
                        case UserAccessLog.ACCESS_TOKEN_EXPIRE_AT:
                            finalAccess.setAccessTokenExpireAt(value);
                            break;
                        case UserAccessLog.REFRESH_TOKEN_EXPIRE_AT:
                            finalAccess.setRefreshTokenExpireAt(value);
                            break;
                        default:
                            log.error(String.format("Unknown field '%s' for UserAccess", field));
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
