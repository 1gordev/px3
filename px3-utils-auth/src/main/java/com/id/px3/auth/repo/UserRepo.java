package com.id.px3.auth.repo;

import com.id.px3.auth.model.entity.User;
import com.id.px3.utils.mongo.IndexUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserRepo {

    private final MongoTemplate mongoTemplate;

    public UserRepo(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }


    @PostConstruct
    public void init() {
        IndexUtils.ensureIndexes(mongoTemplate, User.class);

        log.info("UserRepo initialized");
    }

    /**
     * Find user by username
     *
     * @param username - username
     * @return The user if found
     */
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(mongoTemplate.findOne(
                new Query(Criteria.where(User.USERNAME).is(username)),
                User.class
        ));
    }

    /**
     * Find user by id
     *
     * @param userId - user id
     * @return The user if found
     */
    public Optional<User> findById(String userId) {
        return Optional.ofNullable(mongoTemplate.findById(userId, User.class));
    }

    /**
     * Count the number of Users in the collection
     *
     * @return The number of Users
     */
    public long count() {
        return mongoTemplate.count(new Query(), User.class);
    }

    /**
     * Save a User
     *
     * @param user - The User to save
     * @return The saved User
     */
    public User save(User user) {
        return mongoTemplate.save(user);
    }

}
