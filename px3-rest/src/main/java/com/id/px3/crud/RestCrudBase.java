package com.id.px3.crud;

import com.id.px3.rest.RestControllerBase;
import com.id.px3.rest.security.JwtSecured;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
public abstract class RestCrudBase<T, K> extends RestControllerBase {

    protected abstract IAccessControlBase<T, K> provideAccessControl();

    protected abstract ICrudServiceBase<T, K> provideService();

    protected abstract ICrudValidator<T> provideValidator();

    @GetMapping
    @JwtSecured
    public ResponseEntity<List<T>> findAll() {
        log.trace("findAll()");
        provideAccessControl().canFindAll(getUserId(), getAuthToken());
        List<T> list = provideService().findAll();
        log.trace("findAll() returns {} list", list);
        return ResponseEntity.ok(list);
    }

    @PostMapping("action/{name}")
    @JwtSecured
    public ResponseEntity<List<T>> action(@PathVariable("name") String name, @RequestBody Map<String, Object> params) {
        log.trace("action({}, {})", name, params);
        provideAccessControl().canDoAction(getUserId(), getAuthToken(), name, params);
        List<T> list = provideService().processAction(name, params);
        log.trace("action({}, {}) returns {} list", name, list, list);
        return ResponseEntity.ok(list);
    }

    @GetMapping("{id}")
    @JwtSecured
    public ResponseEntity<T> findById(@PathVariable("id") K id) {
        log.trace("findById({})", id);
        provideAccessControl().canFindById(getUserId(), getAuthToken(), id);
        T entity = provideService().findById(id);
        log.trace("findById({}) returns {} entity", id, entity);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entity);
    }

    @PostMapping("by-ids")
    @JwtSecured
    public ResponseEntity<List<T>> findByIds(@RequestBody List<K> ids) {
        log.trace("findByIds({})", ids);
        provideAccessControl().canFindByIds(getUserId(), getAuthToken(), ids);
        List<T> list = provideService().findByIds(ids);
        log.trace("findByIds({}) returns {} list", ids, list);
        return ResponseEntity.ok(list);
    }

    @PostMapping
    @JwtSecured
    public ResponseEntity<T> create(@RequestBody T entity) {
        log.trace("create({})", entity);
        provideAccessControl().canCreate(getUserId(), getAuthToken(), entity);
        provideValidator().beforeCreate(entity);
        T saved = provideService().save(entity);
        log.trace("create({}) returns {} saved", entity, saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("{id}")
    @JwtSecured
    public ResponseEntity<T> update(@PathVariable("id") K id, @RequestBody T entity) {
        log.trace("update({}, {})", id, entity);
        provideAccessControl().canUpdate(getUserId(), getAuthToken(), id, entity);
        provideValidator().beforeUpdate(entity);
        T updated = provideService().update(id, entity);
        log.trace("update({}, {}) returns {} updated", id, entity, updated);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("{id}")
    @JwtSecured
    public ResponseEntity<Void> delete(@PathVariable("id") K id) {
        log.trace("delete({})", id);
        provideAccessControl().canDelete(getUserId(), getAuthToken(), id);
        provideService().delete(id);
        log.trace("delete({}) done", id);
        return ResponseEntity.noContent().build();
    }
}
