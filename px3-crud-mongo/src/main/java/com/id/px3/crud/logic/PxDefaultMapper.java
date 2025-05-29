package com.id.px3.crud.logic;

import com.id.px3.crud.IPxMapperBase;
import org.springframework.beans.BeanUtils;

public class PxDefaultMapper<T, E> implements IPxMapperBase<T, E> {

    private final Class<T> modelClass;
    private final Class<E> entityClass;

    public PxDefaultMapper(Class<T> modelClass, Class<E> entityClass) {
        this.modelClass = modelClass;
        this.entityClass = entityClass;
    }

    @Override
    public Class<T> provideModelClass() {
        return modelClass;
    }

    @Override
    public Class<E> provideEntityClass() {
        return entityClass;
    }

    @Override
    public E toEntity(T model) {
        if (model == null) {
            return null;
        }
        try {
            E entity = entityClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(model, entity);
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Error creating entity instance", e);
        }
    }

    @Override
    public T toModel(E entity) {
        if (entity == null) {
            return null;
        }
        try {
            T model = modelClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(entity, model);
            return model;
        } catch (Exception e) {
            throw new RuntimeException("Error creating model instance", e);
        }
    }
}
