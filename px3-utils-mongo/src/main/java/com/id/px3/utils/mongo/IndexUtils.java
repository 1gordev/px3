package com.id.px3.utils.mongo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

@Slf4j
public class IndexUtils {

    /**
     * Ensure indexes for a document class.
     * For Collection type fields, ensure indexes for their fields as well.
     *
     * @param mongoTemplate - MongoTemplate
     * @param clazz - Document class
     * @param additionalFields - Additional fields to ensure indexes for
     * @param documentFields - Document fields to ensure indexes for
     * @param <T> - Document class type
     */
    public static <T> void ensureIndexes(MongoTemplate mongoTemplate,
                                         Class<T> clazz,
                                         List<Field> additionalFields,
                                         List<String> documentFields) {

        //  ensure clazz is a @Document
        if (!clazz.isAnnotationPresent(Document.class)) {
            throw new IllegalArgumentException("Class must be annotated with @Document");
        }

        //  ensure indexes for fields annotated with @Indexed
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Indexed.class)) {
                ensureFieldIndex(mongoTemplate, clazz, field);
            } else {
                //  for collections, ensure indexes for their fields
                if (Collection.class.isAssignableFrom(field.getType())) {
                    try {
                        Class<?> genericType = (Class<?>) ((java.lang.reflect.ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        ensureEmbeddedIndexes(mongoTemplate, genericType, field.getName());
                    } catch (Exception e) {
                        log.error("Error ensuring indexes for field: %s".formatted(field.getName()), e);
                    }
                }
            }

        }

        //  ensure indexes for additional fields
        if (additionalFields != null) {
            for (Field field : additionalFields) {
                if (field.isAnnotationPresent(Indexed.class)) {
                    ensureFieldIndex(mongoTemplate, clazz, field);
                }
            }
        }

        //  ensure indexes for document fields
        if (documentFields != null) {
            for (String field : documentFields) {
                Index idx = new Index().on(field, Sort.Direction.ASC);
                mongoTemplate.indexOps(clazz).ensureIndex(idx);
            }
        }
    }

    private static void ensureEmbeddedIndexes(MongoTemplate mongoTemplate, Class<?> clazz, String prefix) {
        //  ensure indexes for fields annotated with @Indexed
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Indexed.class)) {
                ensureFieldIndex(mongoTemplate, clazz, field);
            } else {
                //  for collections, ensure indexes for their fields
                if (Collection.class.isAssignableFrom(field.getType())) {
                    try {
                        Class<?> genericType = (Class<?>) ((java.lang.reflect.ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                        ensureEmbeddedIndexes(mongoTemplate, genericType, "%s.%s".formatted(prefix, field.getName()));
                    } catch (Exception e) {
                        log.error("Error ensuring indexes for field: %s".formatted(field.getName()), e);
                    }
                }
            }
        }
    }

    /**
     * Ensure indexes for a document class
     *
     * @param mongoTemplate - MongoTemplate
     * @param clazz - Document class
     * @param <T> - Document class type
     */
    public static <T> void ensureIndexes(MongoTemplate mongoTemplate, Class<T> clazz) {
        ensureIndexes(mongoTemplate, clazz, null, null);
    }

    private static <T> void ensureFieldIndex(MongoTemplate mongoTemplate, Class<T> clazz, Field field) {
        Indexed indexed = field.getAnnotation(Indexed.class);
        Index idx = new Index().on(
                field.getName(),
                indexed.direction() == IndexDirection.ASCENDING
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC
        );
        if (indexed.unique()) {
            idx = idx.unique();
        }
        mongoTemplate.indexOps(clazz).ensureIndex(idx);
    }

}
