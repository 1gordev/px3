package com.id.px3.crud;

import com.id.px3.rest.PxRestControllerBase;
import com.id.px3.rest.security.JwtSecured;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
public abstract class PxRestCrudBase<T, K> extends PxRestControllerBase {

    private IPxAccessControlBase<T, K> accessControl;
    private IPxCrudServiceBase<T, ?, K> crudService;
    private IPxCrudValidator<T> validator;

    protected abstract IPxAccessControlBase<T, K> provideAccessControl();

    protected abstract IPxCrudServiceBase<T, ?, K> provideCrudService();

    protected abstract IPxCrudValidator<T> provideValidator();


    @GetMapping
    @JwtSecured
    public ResponseEntity<List<T>> findAll() {
        log.trace("findAll()");
        provideCachedAccessControl().canFindAll(getUserId(), getAuthToken());
        List<T> list = provideCachedCrudService().findAll();
        log.trace("findAll() returns {} list", list);
        return ResponseEntity.ok(list);
    }

    @PostMapping("action/{name}")
    @JwtSecured
    public ResponseEntity<List<T>> action(@PathVariable("name") String name, @RequestBody Map<String, Object> params) {
        log.trace("action({}, {})", name, params);
        provideCachedAccessControl().canDoAction(getUserId(), getAuthToken(), name, params);
        List<T> list = provideCachedCrudService().processAction(name, params);
        log.trace("action({}, {}) returns {} list", name, list, list);
        return ResponseEntity.ok(list);
    }

    @GetMapping("{id}")
    @JwtSecured
    public ResponseEntity<T> findById(@PathVariable("id") K id) {
        log.trace("findById({})", id);
        provideCachedAccessControl().canFindById(getUserId(), getAuthToken(), id);
        T entity = provideCachedCrudService().findById(id);
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
        provideCachedAccessControl().canFindByIds(getUserId(), getAuthToken(), ids);
        List<T> list = provideCachedCrudService().findByIds(ids);
        log.trace("findByIds({}) returns {} list", ids, list);
        return ResponseEntity.ok(list);
    }

    @PostMapping
    @JwtSecured
    public ResponseEntity<T> create(@RequestBody T entity) {
        log.trace("create({})", entity);
        provideCachedAccessControl().canCreate(getUserId(), getAuthToken(), entity);
        provideCachedValidator().beforeCreate(entity);
        T saved = provideCachedCrudService().save(entity);
        log.trace("create({}) returns {} saved", entity, saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("{id}")
    @JwtSecured
    public ResponseEntity<T> update(@PathVariable("id") K id, @RequestBody T entity) {
        log.trace("update({}, {})", id, entity);
        provideCachedAccessControl().canUpdate(getUserId(), getAuthToken(), id, entity);
        provideCachedValidator().beforeUpdate(entity);
        T updated = provideCachedCrudService().update(id, entity);
        log.trace("update({}, {}) returns {} updated", id, entity, updated);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("{id}")
    @JwtSecured
    public ResponseEntity<Void> delete(@PathVariable("id") K id) {
        log.trace("delete({})", id);
        provideCachedAccessControl().canDelete(getUserId(), getAuthToken(), id);
        provideCachedCrudService().delete(id);
        log.trace("delete({}) done", id);
        return ResponseEntity.noContent().build();
    }

    private IPxAccessControlBase<T, K> provideCachedAccessControl() {
        if (accessControl == null) {
            accessControl = provideAccessControl();
            log.info("%s - Access control: %s".formatted(getClass().getSimpleName(), accessControl));
        }
        return accessControl;
    }

    private IPxCrudServiceBase<T, ?, K> provideCachedCrudService() {
        if (crudService == null) {
            crudService = provideCrudService();
            log.info("%s - CRUD service: %s".formatted(getClass().getSimpleName(), crudService));
        }
        return crudService;
    }

    private IPxCrudValidator<T> provideCachedValidator() {
        if (validator == null) {
            validator = provideValidator();
            log.info("%s - Validator: %s".formatted(getClass().getSimpleName(), validator));
        }
        return validator;
    }
}
