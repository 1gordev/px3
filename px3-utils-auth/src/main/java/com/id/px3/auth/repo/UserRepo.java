package com.id.px3.auth.repo;

import com.id.px3.auth.model.entity.User;
import com.id.px3.auth.model.factory.UserFactory;
import com.id.px3.model.auth.UserDto;
import com.id.px3.utils.mongo.IndexUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

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
                new Query(where(User.USERNAME).is(username)),
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

    /**
     * Delete a User by id
     *
     * @param userId - user id
     */
    public void deleteById(String userId) {
        mongoTemplate.remove(new Query(where(User.ID).is(userId)), User.class);
    }

    /**
     * Find users by id, roles and indexed properties
     *
     * @param ids - user ids
     * @param roles - user roles
     * @param indexedProps - indexed properties
     *
     * @return The list of users
     */
    public List<UserDto> findByIdInAndRolesInAndIndexedProps(List<String> ids, List<String> roles, List<String> indexedProps) {
        return mongoTemplate.find(
                new Query(
                        new Criteria().andOperator(
                                where(User.ID).in(ids),
                                where(User.ROLES).in(roles),
                                where(User.INDEXED_PROPS).in(indexedProps)
                        )),
                User.class
        ).stream().map(UserFactory::toDto).toList();
    }

    /**
     * Find users by id
     *
     * @param ids - user ids
     *
     * @return The list of users
     */
    public List<UserDto> findByIdIn(List<String> ids) {
        return mongoTemplate.find(query(where(User.ID).in(ids)), User.class).stream().map(UserFactory::toDto).toList();
    }

    /**
     * Find users by roles
     *
     * @param roles - user roles
     *
     * @return The list of users
     */
    public List<UserDto> findByRolesIn(List<String> roles) {
        return mongoTemplate.find(query(where(User.ROLES).in(roles)), User.class).stream().map(UserFactory::toDto).toList();
    }

    /**
     * Find users by indexed properties
     *
     * @param indexedProps - indexed properties
     *
     * @return The list of users
     */
    public List<UserDto> findByIndexedProps(List<String> indexedProps) {
        return mongoTemplate.find(query(where(User.INDEXED_PROPS).in(indexedProps)), User.class).stream().map(UserFactory::toDto).toList();
    }

    public List<UserDto> findByIdInAndIndexedProps(List<String> ids, List<String> indexedProps) {
        return mongoTemplate.find(
                new Query(
                        new Criteria().andOperator(
                                where(User.ID).in(ids),
                                where(User.INDEXED_PROPS).in(indexedProps)
                        )),
                User.class
        ).stream().map(UserFactory::toDto).toList();
    }

    /**
     * Find users by roles and indexed properties
     *
     * @param roles - user roles
     * @param indexedProps - indexed properties
     *
     * @return The list of users
     */
    public List<UserDto> findByRolesInAndIndexedProps(List<String> roles, List<String> indexedProps) {
        return mongoTemplate.find(
                new Query(
                        new Criteria().andOperator(
                                where(User.ROLES).in(roles),
                                where(User.INDEXED_PROPS).in(indexedProps)
                        )),
                User.class
        ).stream().map(UserFactory::toDto).toList();
    }

    /**
     * Find users by id and roles
     *
     * @param ids - user ids
     * @param roles - user roles
     *
     * @return The list of users
     */
    public List<UserDto> findByIdInAndRolesIn(List<String> ids, List<String> roles) {
        return mongoTemplate.find(
                new Query(
                        new Criteria().andOperator(
                                where(User.ID).in(ids),
                                where(User.ROLES).in(roles)
                        )),
                User.class
        ).stream().map(UserFactory::toDto).toList();
    }

    /**
     * Find all users
     *
     * @return The list of users
     */
    public List<UserDto> findAll() {
        return mongoTemplate.findAll(User.class).stream().map(UserFactory::toDto).toList();
    }
}
