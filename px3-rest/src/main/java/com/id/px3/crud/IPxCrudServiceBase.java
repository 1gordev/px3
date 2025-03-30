package com.id.px3.crud;

import java.util.List;
import java.util.Map;

public interface IPxCrudServiceBase<T, E, K> {

    Class<E> provideEntityClass();

    List<T> findAll();

    T findById(K id);

    List<T> findByIds(List<K> ids);

    T save(T entity);

    T update(K id, T entity);

    void delete(K id);

    List<T> processAction(String name, Map<String, Object> params);

}
