package com.id.px3.auth.repo;

import com.id.px3.auth.model.entity.UserRole;
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
public class UserRoleRepo {

    private final MongoTemplate mongoTemplate;

    public UserRoleRepo(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void init() {
        IndexUtils.ensureIndexes(mongoTemplate, UserRole.class);

        log.info("RoleRepo initialized");
    }

    /**
     * Find role by code
     *
     * @param code - role code
     * @return The role if found
     */
    public Optional<UserRole> findByCode(String code) {
        return Optional.ofNullable(mongoTemplate.findOne(
                new Query(Criteria.where(UserRole.CODE).is(code)),
                UserRole.class
        ));
    }

    /**
     * Save or update a role
     *
     * @param r - The role to save
     * @return The saved role
     */
    public UserRole save(UserRole r) {
        return mongoTemplate.save(r);
    }
}
