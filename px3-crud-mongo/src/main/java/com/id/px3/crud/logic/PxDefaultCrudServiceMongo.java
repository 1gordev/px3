package com.id.px3.crud.logic;

import com.id.px3.crud.IPxCrudServiceBase;
import com.id.px3.crud.IPxMapperBase;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Getter
@Slf4j
public abstract class PxDefaultCrudServiceMongo<T, E, K> implements IPxCrudServiceBase<T, E, K> {

    public static final String DEFAULT_COLLECTION_NAME = null;
    private final Class<E> entityClass;
    private final String collectionName;
    private final Field idField;
    private final MongoTemplate mongoTemplate;
    private final IPxMapperBase<T, E> mapper;

    @Override
    public abstract List<T> processAction(String name, Map<String, Object> params);

    public PxDefaultCrudServiceMongo(MongoTemplate mongoTemplate,
                                     IPxMapperBase<T, E> mapper,
                                     String collectionName) {
        this.mongoTemplate = mongoTemplate;
        this.mapper = mapper;

        this.entityClass = provideEntityClass();

        this.collectionName = (collectionName == null || collectionName.isBlank())
                ? detectCollectionName(entityClass)
                : collectionName;

        this.idField = detectIdField(entityClass);
    }

    @Override
    public List<T> findAll() {
        List<E> entities = mongoTemplate.findAll(entityClass, collectionName);
        return entities.stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public T findById(K id) {
        E entity = mongoTemplate.findById(id, entityClass, collectionName);
        return (entity == null) ? null : mapper.toModel(entity);
    }

    @Override
    public List<T> findByIds(List<K> ids) {
        Query query = new Query();
        query.addCriteria(where(idField.getName()).in(ids));
        List<E> entities = mongoTemplate.find(query, entityClass, collectionName);
        return entities.stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public T save(T model) {
        // Map model to entity for saving
        E entity = mapper.toEntity(model);

        // Create id if not present
        try {
            Object entityId = idField.get(entity);
            if (entityId == null || (entityId instanceof String && entityId.toString().isBlank())) {
                idField.set(entity, UUID.randomUUID().toString());
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set id on entity", e);
        }

        E savedEntity = mongoTemplate.save(entity, collectionName);

        // Map back to model after save
        return mapper.toModel(savedEntity);
    }

    @Override
    @Transactional
    public T update(K id, T model) {
        // Map model to entity for updating
        E entity = mapper.toEntity(model);

        // Set id (avoid overwriting the id field in the entity)
        try {
            idField.set(entity, id);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set id on entity", e);
        }

        // Replace the existing document with the new entity using findAndReplace.
        E savedEntity = mongoTemplate.findAndReplace(
                query(where(idField.getName()).is(id)),
                entity,
                FindAndReplaceOptions.options().returnNew(),
                collectionName
        );

        // Map back to model and return it.
        return mapper.toModel(savedEntity);
    }

    @Override
    public void delete(K id) {
        E entity = mongoTemplate.findById(id, entityClass, collectionName);
        if (entity != null) {
            mongoTemplate.remove(entity, collectionName);
        }
    }

    @Override
    public Class<E> provideEntityClass() {
        if (mapper == null) {
            var err = "IPxMapper implementation not provided";
            log.error(err);
            throw new RuntimeException(err);
        }
        return mapper.provideEntityClass();
    }

    /**
     * Automatically detects the collection name from the @Document annotation.
     * If the annotation is present and its "collection" property is not empty,
     * that value is returned. Otherwise, the entity class's simple name is used.
     *
     * @param clazz the entity class.
     * @return the detected collection name.
     */
    private String detectCollectionName(Class<?> clazz) {
        Document document = clazz.getAnnotation(Document.class);
        if (document != null && !document.collection().isEmpty()) {
            return document.collection();
        }
        return clazz.getSimpleName();
    }

    /**
     * Finds and caches the field annotated with @Id for the given class.
     *
     * @param clazz the class to search for an @Id field.
     * @return the Field annotated with @Id.
     * @throws IllegalArgumentException if no such field is found.
     */
    private Field detectIdField(Class<?> clazz) {
        // Check declared fields in the class
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                return field;
            }
        }
        // Check superclasses if not found
        Class<?> superclass = clazz.getSuperclass();
        while (superclass != null && superclass != Object.class) {
            for (Field field : superclass.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    field.setAccessible(true);
                    return field;
                }
            }
            superclass = superclass.getSuperclass();
        }
        throw new IllegalArgumentException("No field annotated with @Id found in class " + clazz.getName());
    }
}
