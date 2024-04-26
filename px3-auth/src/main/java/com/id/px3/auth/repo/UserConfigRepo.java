package com.id.px3.auth.repo;

import com.id.px3.auth.model.entity.UserConfig;
import com.id.px3.utils.mongo.IndexUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserConfigRepo {

    private final MongoTemplate mongoTemplate;

    public UserConfigRepo(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void init() {
        IndexUtils.ensureIndexes(mongoTemplate, UserConfig.class);
        log.info("ConfigRepo initialized");
    }

    /**
     * Find config by code
     *
     * @param code - config code
     * @return The config if found
     */
    public Optional<UserConfig> findByCode(String code) {
        return Optional.ofNullable(mongoTemplate.findOne(
                new Query(Criteria.where(UserConfig.CODE).is(code)),
                UserConfig.class
        ));
    }

    /**
     * Save or update a config
     *
     * @param r - The config to save
     * @return The saved config
     */
    public UserConfig save(UserConfig r) {
        return mongoTemplate.save(r);
    }
}
