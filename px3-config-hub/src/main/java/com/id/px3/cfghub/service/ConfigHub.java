package com.id.px3.cfghub.service;

import com.id.px3.cfghub.ConfigPathEntity;
import com.id.px3.cfghub.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class ConfigHub {

    private static Logger log = LoggerFactory.getLogger(ConfigHub.class);

    private final AppConfig appConfig;
    private final MongoTemplate mongoTemplate;

    public ConfigHub(AppConfig appConfig, MongoTemplate mongoTemplate) {
        this.appConfig = appConfig;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Put a value in the config hub.
     *
     * @param path - the path to the value
     * @param value - the value to put
     */
    public void put(String path, String value) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        var query = query(where("path").regex("^" + Pattern.quote(path) + "$", "i"));
        if (value == null) {
            // Remove the key
            mongoTemplate.remove(query, appConfig.getConfigHubCollection());
        } else {
            // Upsert the key
            mongoTemplate.upsert(
                    query,
                    new org.springframework.data.mongodb.core.query.Update().set("value", value),
                    appConfig.getConfigHubCollection()
            );
        }
    }

    /**
     * Get a value from the config hub.
     *
     * @param path - the path to the value
     * @return the value or null if not found
     */
    public String get(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }

        var result = mongoTemplate.findOne(
                query(where("path").regex("^" + Pattern.quote(path) + "$", "i")),
                ConfigPathEntity.class, appConfig.getConfigHubCollection());

        return result != null ? result.getValue() : null;
    }

}
