package com.id.px3.crud;

public interface IPxCrudValidator<T> {

    void beforeCreate(T entity);

    void beforeUpdate(T entity);

}
