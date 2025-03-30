package com.id.px3.crud;

import java.util.List;
import java.util.Map;

public interface IPxAccessControlBase<T, K> {

    void canFindAll(String userId, String authToken);

    void canFindById(String userId, String authToken, K id);

    void canFindByIds(String userId, String authToken, List<K> ids);

    void canCreate(String userId, String authToken, T entity);

    void canUpdate(String userId, String authToken, K id, T entity);

    void canDelete(String userId, String authToken, K id);

    void canDoAction(String userId, String authToken, String name, Map<String, Object> params);
}
