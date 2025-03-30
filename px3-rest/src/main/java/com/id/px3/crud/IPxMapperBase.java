package com.id.px3.crud;

public interface IPxMapperBase <T, E> {

    Class<T> provideModelClass();
    Class<E> provideEntityClass();

    E toEntity(T model);
    T toModel(E entity);

}
