package com.id.px3.crud.validation;

import com.id.px3.crud.IPxCrudValidator;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;
import java.util.Set;

@Slf4j
public class PxDefaultValidator<T> implements IPxCrudValidator<T> {

    private final Validator validator;

    public PxDefaultValidator(Validator validator) {
        this.validator = validator;
    }

    @Override
    public void beforeCreate(T entity) {
        validate(entity);
    }

    @Override
    public void beforeUpdate(T entity) {
        validate(entity);
    }

    private void validate(T entity) {
        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder("Validation errors: ");
            for (ConstraintViolation<T> violation : violations) {
                sb.append(violation.getPropertyPath())
                        .append(" ")
                        .append(violation.getMessage())
                        .append("; ");
            }
            log.error(sb.toString());
            throw new RuntimeException("Validation failed");
        }
    }
}
